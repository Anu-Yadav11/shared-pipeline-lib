def call(string status, Map opts =[:]){
    def recipients = opts.to ?: 'a367.ay@gmail.com, anu93071@gmail.com'
    def attachPattern = opts.attachmentsPattern ?: 'build-report.txt'

    def reportText = """\
    Jenkins Build Report
    =====================

    job : ${env.JOB_NAME}
    build : #${env.BUILD_NUMBER}
    result : ${status}
    url : ${env.BUILD_URL}
    branch : ${env.BRANCH_NAME ?: 'N/A'}
    user : ${env.BUILD_USER ?: 'N/A'}

    """

    try {
        writeFile file: 'build-report.txt', text: reportText
        echo "wrote build-report.txt to workspace"
        
    }
    catch (e) {
        echo "failed to write buil-report.txt: $(e)"
    }

    def color =(status== "success") ? "green" : "red"

    def htmlBody = """<html>
  <body>
    <p>Hello team,</p>
    <p>
      <b>Job:</b> ${env.JOB_NAME}<br/>
      <b>Build:</b> ${env.BUILD_NUMBER}<br/>
      <b>Status:</b> <span style="color:${color}; font-weight:bold;">${status}</span><br/>
      <b>Branch:</b> ${env.BRANCH_NAME ?: 'N/A'}<br/>
      <b>URL:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a>
    </p>
    <p>Attached: build-report.txt (summary)</p>
    <p>-- Jenkins</p>
  </body>
</html>"""

try {
    emailext(
      to: recipients,
      subject: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
      body: htmlBody,
      mimeType: 'text/html',
      attachmentsPattern: attachPattern
    )
    echo "emailext sent to: ${recipients}"
    return true
  } catch (emailextEx) {
    echo "emailext failed: ${emailextEx}. Falling back to plain mail(...)"
    // Fallback: use mail (plain text) so at least notification arrives
    try {
      mail(
        to: recipients,
        subject: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        body: reportText
      )
      echo "Fallback mail sent to: ${recipients}"
      return true
    } catch (mailEx) {
      echo "Fallback mail also failed: ${mailEx}"
      error "Both emailext and mail failed to send notification"
    }
  }


}
