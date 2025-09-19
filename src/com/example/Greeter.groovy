package com.example
class Greeter implements Serializable {
  def steps
  Greeter(steps) { this.steps = steps }
  String greet(String name) {
    if (!name) return "Hello, stranger!"
    return "Hello, ${name} from Greeter!"
  }
}

