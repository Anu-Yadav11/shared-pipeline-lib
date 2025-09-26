// vars/notifyShift.groovy
def call(Map opts = [:]) {
  def name   = opts.name ?: 'Unknown'
  def day    = opts.day ?: '??'
  def month  = opts.month ?: '??'
  def timing = opts.shift_timing ?: 'Not given'
  def status = opts.shift_status ?: 'start'

  def msg = """${name} shift ${status}ed
Day: ${day}, Month: ${month}
Timing: ${timing}
"""

  // Save shift report
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
  //archiveArtifacts artifacts: 'shift-report.txt'

  // Append to history log
  sh """echo '${report}' >> shift-history.log"""
 // archiveArtifacts artifacts: 'shift-history.log'

  // Telegram
  def botToken = "<YOUR_BOT_TOKEN>"
  def chatId   = "<YOUR_GROUP_CHAT_ID>"
  sh """
    curl -s -X POST https://api.telegram.org/bot${botToken}/sendMessage \
    -d chat_id=${chatId} \
    -d text="${msg}"
  """

  // MongoDB Insert (requires mongosh installed on Jenkins node/agent)
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
    echo '${jsonDoc}' | mongosh "mongodb://admin:admin@localhost:27017/shiftsDB" --quiet --eval 'const doc=JSON.parse(cat("/dev/stdin")); db.shifts.insertOne(doc);'
  """

  echo "Shift notification + DB record saved for ${name}"
}
