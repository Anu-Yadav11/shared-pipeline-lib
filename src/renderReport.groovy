def call(Map args = [:]) {
  String project = args.project ?: 'unknown'
  String branch = args.branch ?: env.BRANCH_NAME ?: 'local'
  String result = args.result ?: 'UNKNOWN'
  def r = new com.example.ReportUtils(this)
  def txt = r.render(project, branch, result)
  writeFile file: 'build-report.txt', text: txt
  archiveArtifacts artifacts: 'build-report.txt'
  return txt
}

