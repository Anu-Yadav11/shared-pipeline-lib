// vars/notify.groovy
def call(String status) {
  def recipients = 'a367.ay@gmail.com, anu93071@gmail.com'

  echo "notify called with status = ${status}, recipients = ${recipients}"

  // decide color based on status
  def color = (status == "SUCCESS") ? "green" : "red"

  // send HTML email with color
  emailext(
    to: recipients,
    subject: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
    body: """
<html>
  <body>
    <p>Hello team,</p>

    <p>
      <b>Job:</b> ${env.JOB_NAME}<br/>
      <b>Build:</b> ${env.BUILD_NUMBER}<br/>
      <b>Status:</b> <span style="color:${color}; font-weight:bold;">${status}</span><br/>
      <b>URL:</b> <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a>
    </p>

    <p>-- Jenkins</p>
  </body>
</html>
""",
    mimeType: 'text/html'
  )
}
