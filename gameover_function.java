private boolean checkGameOver() {
        /* TODO check if the game is over */
        boolean over = false;
        // test for each row
        for (int i = 0; i < buttons.length; i++)
            for (int j = 0; j < buttons[i].length * buttons[i].length ; j+=3)
                if(buttons[i][j].equals(buttons[i][j+1]) && buttons[i][j+1].equals(buttons[i][j+2]))
                    over = true;
                else if(buttons[j][i].equals(buttons[j+1][i]) && buttons[j+1][i].equals(buttons[j+2][i]));
                    over = true;

        if(buttons[0][0].equals(buttons[1][1]) && buttons[1][1].equals(buttons[2][2]))
            over = true;

        if(buttons[0][2].equals(buttons[1][1]) && buttons[1][1].equals(buttons[2][0]))
            over = true;

        return false;
    }