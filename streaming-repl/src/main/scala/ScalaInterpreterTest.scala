import scala.tools.nsc.interpreter.ILoop

class ScalaInterpreterTest {


}


object ScalaInterpreterTest {

  def main(args: Array[String]): Unit = {
    val iloop = new ILoop();
    iloop.printWelcome();

  }
}