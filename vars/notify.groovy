def call(string status) {
    def recipients='a367.ay@gmail.com , anu93071@gmail.com'

    emailtext(
        to: recipients,
        subject: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        body: """Hello team,

        job : ${env.JOB_NAME}
        Build: #${env.BUILD_NUMBER}
        status: ${status}
        URL: ${env.BUILD_URL}console

        -- jenkins
        """,
        mimeType: 'text/plain'
    )
}
