package com.example
class ReportUtils implements Serializable {
  def steps
  ReportUtils(steps) { this.steps = steps }
  String render(String project, String branch, String result) {
    def tmpl = steps.libraryResource('templates/report.txt')
    tmpl = tmpl.replace('{{project}}', project)
               .replace('{{branch}}', branch)
               .replace('{{result}}', result)
    return tmpl
  }
}

