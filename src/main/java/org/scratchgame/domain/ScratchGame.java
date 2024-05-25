package org.scratchgame.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.scratchgame.model.Input;
import org.scratchgame.model.Output;
import org.scratchgame.model.ScratchGameConfiguration;

import java.io.File;

@Slf4j
public class ScratchGame {
    @SneakyThrows
    public void play(Parameters parameters) {
        ObjectMapper objectMapper = new ObjectMapper();
        File configFile = new File(parameters.getConfigPath());
        ScratchGameConfiguration scratchGameConfiguration = objectMapper.readValue(configFile, ScratchGameConfiguration.class);
        Matrix matrix = new ScratchGameGenerator(scratchGameConfiguration).generate();
        Output output = new ScratchGameChecker(scratchGameConfiguration).check(matrix, new Input(parameters.getBettingAmount()));
        log.info("{}", objectMapper.writeValueAsString(output));
    }
}
