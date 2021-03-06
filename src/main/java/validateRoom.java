import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

@WebServlet(name = "validateRoom", value = "/validateRoom")
public class validateRoom extends HttpServlet {

    //Just here for clarity, to not duplicate magical numbers.
    public static final int FourCornersError = 1;
    public static final int DiagonalWallsError = 2;
    public static final int IntersectedWallsError = 3;
    public static final int CoordinateTypeError = 4;

    //about that. In validations specified that order must be clockwise HOWEVER all of examples seems to imply counter-clockwise order to be correct, and clockwise to be invalid.
    //Either validation has a typo, or examples. From point here i will assume that correct order must be counter-clockwise according to correctness of examples, not text validations.
    public static final int WallOrderError = 5;
    public static final int UnspecifiedError = -1;




    class Point
    {
        int X;
        int Y;

        public  Point(int x,int y)
        {
            this.X=x;
            this.Y=y;
        }

    }

    class Wall implements Comparable<Wall>
    {
        Point startPoint;
        Point endPoint;
        Boolean isHorizontal;

        public  Wall(Point wallStart, Point wallEnd, boolean b)
        {
            this.startPoint=wallStart;
            this.endPoint=wallEnd;
            isHorizontal = b;
        }

        public Point GetStartPoint()
        {
            return this.startPoint;
        }


        //That part was needed for Ottmann-Bentley algorithm, but i decided it be an overkill
        @Override
        public int compareTo(Wall x) {
        int result;
        if (this.GetStartPoint().X<x.GetStartPoint().X) result=-1;
        else if (GetStartPoint().X>x.GetStartPoint().X) result =1;
        else result=0;
        return result;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {



        ArrayList<Point> currentRoom = new ArrayList<Point>();

            boolean inputCorrectness = true;

            String[] source; //either we get data from user or from database

        if (request.getParameter("ShowRoom")!=null) source = ((String)request.getAttribute("ShowRoomByID")).split(" ");
            else source= request.getParameter("room").split(",");

            String[] room = source;


            //Empty line will be considered as invalid room. But i'll leave example here
            if (room.length == 0) // let's assign some simple training example to empty line
            {

                room = "(1,1), (1,2),(0,2),(0,3),(2,3),(2,1)".split(","); //should be correct L-shaped room
            }


            //Here we transform user input into needed form of data
            try {
                for (int i = 0; i < room.length; i++) {
                    room[i] = room[i].replaceAll("[^0-9]", "");
                    if (room[i].equals("")) {
                        if (i % 2 == 0) {
                            currentRoom.add(new Point(Integer.parseInt(room[i]), 0));
                        } else currentRoom.get(currentRoom.size() - 1).Y = Integer.parseInt(room[i]);
                    }
                    else inputCorrectness = false;
                }
            }
            catch (NumberFormatException e)
            {
                inputCorrectness=false;
            }


        String responseToClient = "";
        Stack<String> result = new Stack<String>();

        //In case of incorrect data points, error will be shown
        if (inputCorrectness) {
            result = VerifyRoom(currentRoom);


            if (result.size() == 0) {
                responseToClient += "room: [";
                for (Point p : currentRoom) {
                    responseToClient += "{x : " + p.X + ", y : " + p.Y + "},";
                }
                responseToClient += "]";

            } else for (String s : result) {
                responseToClient += s;//result
            }
        }
        else
        {
            result.push(GetErrorDescription(CoordinateTypeError));
            responseToClient+=(GetErrorDescription(CoordinateTypeError));
        }



            //Here to correctly pass variables between pages, and also resize canvas coordinates to look a bit better
            String Sroom = "";
            for (String s : room)
                Sroom += s + " ";



//God, i hate js type correction. How many problems could've be avoided
            request.setAttribute("stringRoomPoints", Sroom);
            request.setAttribute("roomPoints", room);

            // In case of wrongly built room layout we need explanation of what is wrong. For valid rooms pass correct data
            if (result.size()==0) {
                request.setAttribute("Errors", "");
            }
            else request.setAttribute("Errors", responseToClient);




            //If this is a new roo, we must add it to DB first
            if (request.getParameter("AddRoom")!=null)
            request.getRequestDispatcher("/ManageRooms").forward(request,response);
            else request.getRequestDispatcher("/index.jsp").forward(request,response);
    }

    public Stack<String> VerifyRoom(ArrayList<Point> input)
    {

        Stack<String> result = new Stack<>();

        ArrayList<Wall> layout = new ArrayList<Wall>(); //Here to keep initial input unmodified, and for intersection check


        //Note: some validations can be processed together, and that will increase program execution speed.
        // However, for sake of simplicity, testing and for sake of common logic, all validations made as separate as possible to do it without duplicating operations.

        //To have 4 corners room must have at least 4 not-intersected walls. Combined with no-diagonal validation, any room with more than 3 walls and no diagonal walls will be correct.
        if (input.size()<4) result.push(GetErrorDescription(FourCornersError));

        //Next is checking walls for diagonality. And creating forming layout, for later purposes
        //No diagonal walls means that between two point one coordinate must match and one must mismatch.
        int diagonalWallsFound=0; //We need just one diagonal wall to stop searching. But, just in case

        for (int i=1; i<input.size()+1;i++)
        {
            //Those two are current and previous input points, implemented to make code a bit more readable

            Point first = input.get(i-1);
            Point second;
            if (i==input.size())  second = input.get(0);
            else
            second = input.get(i);


            if (first.X!=second.X)
            {
                if (first.Y==second.Y)
                {
                    //we will sort wall points a bit, for later use
                    if (first.X<second.X)
                    layout.add(new Wall(first,second,true));
                    else layout.add(new Wall(second,first,true));
                }
                else  diagonalWallsFound++;


            }
            else
                if (first.Y!=second.Y)
                {
                    if (first.Y<second.Y)
                        layout.add(new Wall(first,second,false));
                    else layout.add(new Wall(second,first,false));
                }
                else result.push(GetErrorDescription(IntersectedWallsError)); //This is a point, not a wall, so i'm unsure what error it is, but it's not correct input for sure

            //else two horizontal walls in a row. It's not incorrect, i guess? Just weird
        }

        if (diagonalWallsFound!=0) result.push(GetErrorDescription(DiagonalWallsError));
        //if sign of area is negative, point have clockwise orientation. Otherwise, counter-clockwise
        //if it's 0, then it's not correct polygon, and it will execute other errors
        if (CheckBuildingOrder(input)>0) result.push(GetErrorDescription(WallOrderError));

        if (checkRoomIntersection(layout)) result.push(GetErrorDescription(IntersectedWallsError));

        //if (result.size()==0) result.add("Room validated. No errors found");
        return result;
    }





    //it checks orientation of whole polygon. It could be done via intersection part of algorithm, but i tried to do all check as separately as possible
    public int CheckBuildingOrder (ArrayList<Point> input)
    {
        //Area of polygon. This is not needed for now, we need just a sign, but it's no real harm to keep it
        int S=0;
        for (int i=0;i<input.size()-1;i++)
        {
            S+=(input.get(i).X*input.get(i+1).Y - input.get(i).Y*input.get(i+1).X);
        }
        S+=input.get(input.size()-1).X*input.get(0).Y-input.get(input.size()-1).Y*input.get(0).X;
        return (S/2);
    }

    public boolean checkRoomIntersection (ArrayList<Wall> layout)
    {

        //This can be done by Ottmann-Bentley algorithm, but that seems like an overkill, and code already bloated enough. So i'll use O(n^2) solution, that is more transparent and easier to debug
        for (int i=0;i< layout.size();i++)
        {
            Wall w1 = layout.get(i);
            for (int j=i+1;j<layout.size();j++)
            {

                Wall w2 = layout.get(j);
                if (checkIntersection(w1.startPoint,w1.endPoint,w2.startPoint,w2.endPoint)) return true;
            }
        }
        //we do not need to check intersection between last and first points. There will be no errors as long as they are not diagonal.

        return false;
    }

    static int checkOrientation(Point p, Point q, Point r)
    {
        int val = (q.Y - p.Y) * (r.X - q.X) -
                (q.X - p.X) * (r.Y - q.Y);

        if (val == 0) return 0; // colinear

        return (val > 0)? 1: -1; // clock or counterclock wise
    }

    //return true is there is intersection
    static boolean checkIntersection(Point p1, Point q1, Point p2, Point q2)
    {
        // Locate four possible orientations
        int o1 = checkOrientation(p1, q1, p2);
        int o2 = checkOrientation(p1, q1, q2);
        int o3 = checkOrientation(p2, q2, p1);
        int o4 = checkOrientation(p2, q2, q1);

        // General case
        //tldr: intersections appear only if one line  have different orientations for both point of other line, and similar goes for second line
        //HOWEvER we always have intersections on vertexes, and we must ignore those, thus second if, that discard cases when intersection only happens at the ends of both lines
        if (o1 != o2 && o3 != o4) //Here we detect ALL intersections
            if (Math.abs(o1)+Math.abs(o2)+Math.abs(o3)+Math.abs(o4)>2) //Here we discard intersections on adjusted vertexes
            return true;



        return false; // Doesn't fall in any of the above cases
    }

    public String GetErrorDescription (int errorType)
    {
        String result = "";
        switch (errorType)
        {
            case (FourCornersError):
                result="Room must have at least 4 corners. ";
                break;
            case (DiagonalWallsError):
                result="All walls must form right angles. ";
                break;
            case (IntersectedWallsError):
                result="Intersections of walls are not allowed. ";
                break;
            case (CoordinateTypeError):
                result="Coordinates of walls must be integers in a form (x1,y1),(x2,y2), ... ";
                break;
            case (WallOrderError):
                result="Room has infinite area. Walls order must be counter-clockwise. ";
                break;
            case (UnspecifiedError):
                result="Something wrong, i can feel it... ";
                break;
        }
        return result;
    }

}
