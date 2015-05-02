package com.usp.icmc.tictactoe;

import java.util.ArrayList;

public class ThreadController {

    ArrayList<Thread> threads;

    public ThreadController(){
        threads = new ArrayList<>();
    }

    public Thread createNewThread(Runnable r){
        Thread t = new Thread(r);
        threads.add(t);
        return t;
    }

    public void shutDownAllThreads(){
        threads.stream().filter(Thread::isAlive).forEach(Thread::interrupt);
    }

    public void removeThread(int i){
        if(i >= threads.size())
            return;
        if(threads.get(i).isAlive())
            threads.get(i).interrupt();
        threads.remove(i);
    }

}
