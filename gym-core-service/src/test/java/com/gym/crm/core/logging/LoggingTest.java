package com.gym.crm.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.gym.crm.core.entity.Trainee;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.gym.crm.core.factory.TraineeTestFactory.DEFAULT_USERNAME;
import static com.gym.crm.core.factory.TraineeTestFactory.buildTraineeWithUsername;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingTest {

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TraineeServiceImpl service;

    private ListAppender<ILoggingEvent> listAppender;

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(TraineeServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldLogInfoWhenDeleteSucceeds() {
        Trainee trainee = buildTraineeWithUsername(DEFAULT_USERNAME);

        when(traineeRepository.findByUsername(DEFAULT_USERNAME)).thenReturn(Optional.of(trainee));

        service.deleteTraineeByUsername(DEFAULT_USERNAME);

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.INFO);
    }

    @Test
    void shouldLogWarnWhenNotDeleted() {
        when(traineeRepository.findByUsername(DEFAULT_USERNAME)).thenReturn(Optional.empty());

        service.deleteTraineeByUsername(DEFAULT_USERNAME);

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

}
