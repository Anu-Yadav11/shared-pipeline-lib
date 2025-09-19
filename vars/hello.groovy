// (paste the hello.groovy content)
def call(Map args = [:]) {
    String name = args.name ?: 'stranger'
    echo "Hello from shared lib — hi ${name}!"
}
