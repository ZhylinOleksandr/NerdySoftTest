
<%@ page import="java.util.Stack" %>
<%@ page import="java.awt.*" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>


<%
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
%>

<!DOCTYPE html>
<html>
<head>
    <title>Room validation service</title>
</head>
<body>
</h1>
<br/>

<form id= "validateForm" action="validateRoom" method="POST" >
    Room Layout: <input name="room"  />
    <br><br>
    <input type="submit" name= "AddRoom" value="Add New Room" />
    <br><br>
</form>

<form id= "manageForm" action="ManageRooms" method="POST" >
    Room ID: <input name="roomID"  /><br>
    <br><br>
       <input type="submit" name= "DeleteRoom" value="Delete Room by ID" />
    <input type="submit" name= "ShowRoom" value="Draw Room by ID" />
    <br><br>
</form>




<%//wbTp6<0,.uPd
    String[] points = (String[])request.getAttribute("roomPoints");
    if (points == null) points = new String[0];
%>


<% String Errors = (String)request.getAttribute("Errors");
    if (Errors!="" && Errors!=null)
{%>
<br>
Room validation process encountered some errors:<br>
<%=Errors%>
<br>
<%}%>

<canvas id="roomCanvas" width="500" height="500"></canvas>
    <script>
        var canvas = document.getElementById('roomCanvas')
        var ctx = canvas.getContext('2d');
        var polygon = "<%=request.getAttribute("stringRoomPoints")%>".split(" ");

        //var polygon = ["1","1","1","2","0","2","0","3","2","3","2","1"];
        ctx.beginPath();
        ctx.moveTo(parseInt(polygon[0]*20,10), parseInt(polygon[1]*20,10));
        for(item = 2; item < polygon.length-1; item += 2 ){
            ctx.lineTo( parseInt(polygon[item]*20,10), parseInt(polygon[item+1]*20,10))
        }
        ctx.closePath();
        ctx.stroke();

    </script>


<br><br>

<h1>Rooms Avaible</h1>
<table border="1">
    <tr>
        <td>ID</td>
        <td>Layout</td>
    </tr>
    <%
        try{
            connection = DriverManager.getConnection(connectionUrl+database, userid, password);
            statement=connection.createStatement();
            String sql ="select * from Rooms";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()){
    %>
    <tr>
        <td><%=resultSet.getString("RoomID") %></td>
        <td><%=resultSet.getString("Layout") %></td>
    </tr>
    <%
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    %>
</table>









<p></p>
</body>
</html>