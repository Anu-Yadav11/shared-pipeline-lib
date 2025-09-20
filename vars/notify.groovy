// vars/notify.groovy
def call(String status) {
  def recipients = 'a367.ay@gmail.com, anu93071@gmail.com'

  // quick echo so we can confirm library loaded in console
  echo "notify called with status = ${status}, recipients = ${recipients}"

  // send email (requires Email Extension plugin and global SMTP config)
  mail(
    to: recipients,
    subject: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
    body: """Hello team,

Job   : ${env.JOB_NAME}
Build : ${env.BUILD_NUMBER}
Status: ${status}
URL   : ${env.BUILD_URL}console

-- jenkins
""",
    mimeType: 'text/plain'
  )
}

