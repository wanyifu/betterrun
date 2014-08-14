
import mosek.fusion._

class baker (args: Args) extends Job(args)
{   
  val  recipe_data = 
        List(List( 3.0, 5.0 ),
        List( 1.0, 0.5 ),
        List( 1.2, 0.5 ) )

  var input = IterableSource(recipe_data,'coefficient)

  /* Input right now as follows
  List(3.0,5.0)
  List(1.0,0.5)
  List(1.2,0.5)      */

  .groupAll{_.reduce('coefficient -> 'coefficient_set){
    (coefficient_sofar: List[List[Double]], coefficient: List[List[Double]]) => coefficient_sofar ++ coefficient
    }}

    /*Input right now as follows
    List(3.0,5.0,1.0,0.5,1.2,0.5)*/

  

    val collected = input.toArray

    /*Collected as follows
    Array(3.0,5.0,1.0,0.5,1.2,0.5))
    It seems that Scala Array and Java Array are somewhat interchangable through 
    boxing and unboxing*/


    val ingredientnames = Array( "Flour", "Sugar", "Butter" )      
    val stock = Array(150.0,   22.0,    25.0 )
    
    
    val productnames = Array( "Cakes","Breads" )
            
    val revenue = Array(4.0, 6.0 )
    
    
    public static void main(String[] args)
      throws SolutionError
    {
        Matrix recipe = new DenseMatrix(3,2,collected);
        Model M = new Model("Recipe");      
        try
        {
      // "production" defines the amount of each product to bake.
          Variable production = M.variable("production", 
                                           new StringSet(productnames), 
                                           Domain.greaterThan(0.0));
        // The objective is to maximize the total revenue.
          M.objective("revenue",
                      ObjectiveSense.Maximize, 
                      Expr.dot(revenue, production));
        
        // The prodoction is constrained by stock:
          M.constraint(Expr.mul(recipe, production), Domain.lessThan(stock));
          M.setLogHandler(new PrintWriter(System.out));
        
        // We solve and fetch the solution:
          M.solve();
          double[] res = production.level();
          System.out.println("Solution:");
          for (int i = 0; i < res.length; ++i)
          {
              System.out.println(" Number of " + productnames[i] + " : " + res[i]);
          }
          System.out.println(" Revenue : $" + 
                         (res[0] * revenue[0] + res[1] * revenue[1]));        
        }
        finally
        {
          M.dispose();
        }
    }
}