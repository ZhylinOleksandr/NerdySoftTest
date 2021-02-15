import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;

@WebServlet(name = "ManageRooms", value = "/ManageRooms")
public class ManageRooms extends HttpServlet {



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //create connection
        String id = request.getParameter("userid");
        String driver = "com.mysql.jdbc.Driver";
        String connectionUrl = "jdbc:mysql://localhost:3306/";
        String database = "NerdySoft";
        String userid = "root";
        String password = "rybuf2012";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        int resultAffected;
        // end of connection creation

        if (request.getParameter("DeleteRoom")!=null)
        {
            try {
                connection = DriverManager.getConnection(connectionUrl + database, userid, password);
                statement = connection.createStatement();
                String delID = (String)request.getParameter("roomID");

                String sql =" DELETE FROM Rooms WHERE RoomID IN ("+delID+");";
                resultAffected = statement.executeUpdate(sql);

                connection.close();
                request.getRequestDispatcher("/index.jsp").forward(request,response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (request.getParameter("ShowRoom")!=null)
        {
            try {
                connection = DriverManager.getConnection(connectionUrl + database, userid, password);
                statement = connection.createStatement();

                String delID = (String)request.getParameter("roomID");

                String sql =" SELECT Layout FROM Rooms WHERE RoomID="+delID+" LIMIT 1;";
                resultSet = statement.executeQuery(sql);

                if (resultSet.next());
                request.setAttribute("ShowRoomByID", resultSet.getString("Layout"));

                connection.close();
                request.getRequestDispatcher("/validateRoom").forward(request,response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        if (request.getParameter("AddRoom")!=null)
        {
            if (request.getAttribute("Errors")=="") //we only add valid rooms
            try{
                connection = DriverManager.getConnection(connectionUrl+database, userid, password);
                statement=connection.createStatement();
                int maxid = 0;
                resultSet = statement.executeQuery("SELECT max(RoomID) FROM Rooms");
                if(resultSet.next())
                maxid=resultSet.getInt("max(RoomID)")+1;

                String sql ="INSERT INTO Rooms (RoomID, Layout)"+"VALUES ("+ maxid +","+'"'+(String)request.getAttribute("stringRoomPoints")+'"'+");";

                resultAffected = statement.executeUpdate(sql);


                connection.close();

                request.getRequestDispatcher("/index.jsp").forward(request,response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        request.getRequestDispatcher("/index.jsp").forward(request,response);
    }
}
