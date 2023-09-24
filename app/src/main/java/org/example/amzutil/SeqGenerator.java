package org.example.amzutil;

public class SeqGenerator {
    private static SeqGenerator counter_obj = null;
    private static int next_id;
    private static int current_id;

    private SeqGenerator(){
        next_id = 1;
    }

    public int get_cur_id() {
        return current_id;
    }

    public static SeqGenerator getInstance(){
        if (counter_obj == null){
            synchronized(SeqGenerator.class){
                if (counter_obj == null){
                    counter_obj = new SeqGenerator();
                }
            }
        }
        current_id = next_id;
        next_id++;
        return counter_obj;
    }
}
