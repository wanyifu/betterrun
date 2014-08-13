
import mosek._

/*     
       max 5x+3y+2z
       x * hello + y * world + z * goodbye   < = 18
       x * hello - 2 * y * world + 3 * z * goodbye < = 10
       7 * hello + 5 * x * world - 10 * z * goodbye < = 15

       x> 0 y > 0 z > 0
       */




class MapReduce_Solver(args: Args) extends Job(args)
{ 
  val input = TextLine("data/hello.txt")
  val output = TextLine("target/data/tempo.txt")
  
  input
  .read
  .flatMap('line -> 'word){ line : String => line.split("\\s")}
  .groupBy('word){group => group.size}
  .write(output)                                    
  /* As usual, performing the Wordcount to get the wordcount of hello, world, goodbye. In this case, hello = 1, world -2, goodbye = 1*/


  
  
  
    mosek.Env   env = null; 
    mosek.Task task = null; 
 
    
      try 
      { 
        env = new mosek.Env ()
        task = new mosek.Task (env, 3, 3)
        task.set_Stream (mosek.Env.streamtype.log,  
                         new mosek.Stream()  
                         {  
                           public void stream(String msg) { System.out.print(msg) } 
                         })
        
        task.readdata ("target/data/tempo.txt")
        /* I perform the read and assume I can somehow pull the wordcount out, although I haven't seen a good implementation yet
        Wordcounts stored in val hello, world, goodbye

        One way of handling that is to let Scalding output converted to .lp or .mps, see iparam.read_data_format for more details. However
        this conversion still need to hard-coded since I haven't seen any configuration or tool to do that  */


        
        val hello
        val world
        val goodbye


        val c = List (5.0,3.0,2.0)
        /* target function multiplier*/
        asub =       List(List(0, 1, 2), 
                    List(0, 1, 2), 
                    List(0, 1, 2), 
                    )
        aval =      List(List(hello,hello, 7*hello), 
                    List(world, -2 * world, 5 * world), 
                    List(goodbye , 3 *goodbye, -10 * goodbye), 
                    )

        /* These are all Sparse Matrix representation suggested by Mosek, although there is no 0 in the matrix at all*/

  mosek.Env.boundkey 
                 
                 bkc    = List(mosek.Env.boundkey.up, 
                           mosek.Env.boundkey.up, 
                           mosek.Env.boundkey.up)
                 blc  = List(-infinity, 
                      -infinity, 
                      -infinity)
                 buc  = List(18.0, 
                      10.0, 
                      15.0)

        /* setting bound for the right side of the 3 linear equations, stored in Scala List*/
    mosek.Env.boundkey 
            bkc  = List(mosek.Env.boundkey.lo, 
                      mosek.Env.boundkey.lo, 
                      mosek.Env.boundkey.lo, 
                      ) 
               blx  = List(0.0, 
                      0.0, 
                      0.0, 
                      ) 
               bux  = List(+infinity, 
                      +infinity, 
                      +infinity, 
                      )
        /* setting bound for the x, y, z variables, stored in Scala List also */

    /*task.appendcons(3) 
    task.appendvars(3)
    maybe don't need it any more because cons and variable nums already set
    Let the system know how many constraints and variables we are having*/

    var i = 0
    for( i <- 0 to 3){
      task.putci(i,c(i))
      task.putvarbound(i,bkx(i),blx(i),bux(i))
      task.putacol(i,asub(i),aval(i))
    }

    /*Real steps of setting the bound through iterations*/
    var j = 0
    for(j <- 0 to 3) 
        task.putconbound(j,bkc(j),blc(j),buc(j))
        /* Same thing for the task */
 
      
      task.putobjsense(mosek.Env.objsense.maximize)

      /*Let the task know we maximize the target function*/

        // Solve the problem 
        task.optimize ()
 
        // Print a summary of the solution 
        task.solutionsummary (mosek.Env.streamtype.log) 
 
        
        
          // We define the output format to be OPF, and tell MOSEK to 
          // leave out parameters and problem data from the output file. 
          task.putintparam (mosek.Env.iparam.write_data_format,    mosek.Env.dataformat.op.value)
          task.putintparam (mosek.Env.iparam.opf_write_solutions,  mosek.Env.onoffkey.on.value)
          task.putintparam (mosek.Env.iparam.opf_write_hints,      mosek.Env.onoffkey.off.value)
          task.putintparam (mosek.Env.iparam.opf_write_parameters, mosek.Env.onoffkey.off.value) 
          task.putintparam (mosek.Env.iparam.opf_write_problem,    mosek.Env.onoffkey.off.value)
 
          task.writedata (args("final_output")) 
 
      } 
      finally 
      { 
        // Dispose of task and environment 
        if (task != null) task.dispose ()
        if (env  != null)  env.dispose () 
      } 
    } 
  
