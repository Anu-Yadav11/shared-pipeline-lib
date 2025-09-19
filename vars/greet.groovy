def call(Map args = [:]) {
  String name = args.to ?: 'stranger'
  def g = new com.example.Greeter(this)
  def msg = g.greet(name)
  echo msg
  return msg
}

