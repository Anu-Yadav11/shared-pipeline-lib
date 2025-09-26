// vars/notifyShift.groovy
def call(Map opts = [:]) {
  def name   = opts.name ?: 'Unknown'
  def day    = opts.day ?: '??'
  def month  = opts.month ?: '??'
  def timing = opts.shift_timing ?: 'Not given'
  def status = opts.shift_status ?: 'start'

  // ----- Telegram message -----
  def msg = """${name} shift ${status}ed
Day: ${day}, Month: ${month}
Timing: ${timing}
"""

withCredentials([string(credentialsId: 'telegram-token', variable: 'TG_TOKEN'),
                 string(credentialsId: 'telegram-chatid', variable: 'TG_CHAT')]) {
   // inside this block TG_TOKEN and TG_CHAT are available
  sh """
    curl -s -X POST https://api.telegram.org/bot${TG_TOKEN}/sendMessage \
      -d chat_id=${TG_CHAT} \
      -d text="${msg}"
  """
}
   
  //def botToken = "8272985598:AAFZ33GjxAKChoNkveXYNcRX-6hsyAbFtUM"  // replace with your token
  //def chatId   = "-4870913458"                                     // replace with your chat id

  sh """
    curl -s -X POST https://api.telegram.org/bot${botToken}/sendMessage \
      -d chat_id=${chatId} \
      -d text="${msg}"
  """

  // ----- Shift report -----
  def report = """\
Shift Report
============

Person : ${name}
Day    : ${day}
Month  : ${month}
Timing : ${timing}
Status : ${status.toUpperCase()}
Job    : ${env.JOB_NAME}
Build  : ${env.BUILD_NUMBER}
URL    : ${env.BUILD_URL}
"""
  writeFile file: 'shift-report.txt', text: report
  sh """echo '${report}' >> shift-history.log"""

  // ----- MongoDB insert (using docker exec) -----
  def jsonDoc = """{
    "name": "${name}",
    "day": "${day}",
    "month": "${month}",
    "timing": "${timing}",
    "status": "${status}",
    "job": "${env.JOB_NAME}",
    "build": "${env.BUILD_NUMBER}",
    "url": "${env.BUILD_URL}",
    "timestamp": "${new Date().format('yyyy-MM-dd HH:mm:ss')}"
  }"""

  sh """
    docker exec -i mongodb mongosh "mongodb://admin:admin@localhost:27017/shiftsDB?authSource=admin" \
      --quiet --eval 'db.shifts.insertOne(${jsonDoc})'
  """


  echo "Shift notification + DB record saved for ${name}"
}
