import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mytimetable implements Comparable<Mytimetable>{

    String classstarttime;
    String classendtime;

    void printmyinfo(){
        System.out.println(this.classstarttime+"~"+this.classendtime);

    }
    @Override
    public int compareTo(Mytimetable timetable){
        if(timetable.classstarttime.compareTo(classendtime)<0){
            return 1;
        }else if(timetable.classstarttime.compareTo(classendtime)>0){
            return -1;
        }
        return 0;
    }

    void setClassstarttime(String starttime){
        classstarttime=starttime;
    }

    void setClassendtime(String endtime){
        classendtime=endtime;
    }
}
