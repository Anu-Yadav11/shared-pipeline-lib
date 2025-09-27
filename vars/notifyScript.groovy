// vars/notifyShift.groovy
def call(Map opts = [:]) {
  def name   = opts.name ?: 'Unknown'
  def day    = opts.day ?: '??'
  def month  = opts.month ?: '??'
  def timing = opts.shift_timing ?: 'Not given'
  def status = opts.shift_status ?: 'start'

  def now = new Date().format('yyyy-MM-dd HH:mm:ss')
  def mongoOutput = ""

  // ----- Run MongoDB command -----
  if (status == "start") {
    mongoOutput = sh(
      script: """
        docker exec -i mongodb mongosh "mongodb://admin:admin@localhost:27017/shiftsDB?authSource=admin" --quiet --eval '
          const existing = db.shifts.findOne({name: "${name}", day: "${day}", month: "${month}", matched_end: false});
          if (existing) {
            print("⚠️ Shift already started for ${name} on ${day}-${month}, skipping insert.");
          } else {
            db.shifts.insertOne({
              name: "${name}",
              day: "${day}",
              month: "${month}",
              timing: "${timing}",
              start_timestamp: "${now}",
              matched_end: false,
              job: "${env.JOB_NAME}",
              build: "${env.BUILD_NUMBER}",
              url: "${env.BUILD_URL}"
            });
            print("✅ Shift START recorded for ${name}");
          }
        '
      """,
      returnStdout: true
    ).trim()
  } else if (status == "end") {
    mongoOutput = sh(
      script: """
        docker exec -i mongodb mongosh "mongodb://admin:admin@localhost:27017/shiftsDB?authSource=admin" --quiet --eval '
          const openShift = db.shifts.findOneAndUpdate(
            {name: "${name}", day: "${day}", month: "${month}", matched_end: false},
            {\$set: {end_timestamp: "${now}", matched_end: true}}
          );
          if (openShift) {
            print("✅ Shift END recorded for ${name}");
          } else {
            print("⚠️ No active shift found for ${name} on ${day}-${month}, cannot close.");
          }
        '
      """,
      returnStdout: true
    ).trim()
  }

  echo "MongoDB says:\n${mongoOutput}"

  // ----- Send Telegram notification only if DB insert/update happened -----
  if (mongoOutput.contains("✅ Shift START recorded") || mongoOutput.contains("✅ Shift END recorded")) {
    def msg = """${name} shift ${status}ed
Day: ${day}, Month: ${month}
Timing: ${timing}
"""

    withCredentials([
      string(credentialsId: 'telegram-token', variable: 'TG_TOKEN'),
      string(credentialsId: 'telegram-chatid', variable: 'TG_CHAT')
    ]) {
      sh """
        curl -s -X POST https://api.telegram.org/bot${TG_TOKEN}/sendMessage \
          -d chat_id=${TG_CHAT} \
          -d text="${msg}"
      """
    }
  } else {
    echo "⚠️ MongoDB operation skipped; not sending Telegram message."
  }

  // ----- Write report to file -----
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

  echo "✅ Shift notification + DB record saved for ${name}"
}
