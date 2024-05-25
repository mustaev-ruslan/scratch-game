package org.scratchgame;

import com.beust.jcommander.JCommander;
import org.scratchgame.domain.Parameters;
import org.scratchgame.domain.ScratchGame;

public class Main {

    public static void main(String[] args) {
        Parameters parameters = new Parameters();
        new JCommander(parameters).parse(args);
        new ScratchGame().play(parameters);
    }

}