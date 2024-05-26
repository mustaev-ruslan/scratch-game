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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    public void play(Parameters parameters) {
        Input input = new Input(parameters.getBettingAmount());
        InputValidator.validate(input);

        ScratchGameConfiguration scratchGameConfiguration = OBJECT_MAPPER.readValue(
                new File(parameters.getConfigPath()), ScratchGameConfiguration.class);
        ConfigurationValidator.validate(scratchGameConfiguration);

        Matrix matrix = new ScratchGameGenerator(scratchGameConfiguration).generate();
        Output output = new ScratchGameChecker(scratchGameConfiguration).check(matrix, input);
        log.info("{}", OBJECT_MAPPER.writeValueAsString(output));
    }
}
