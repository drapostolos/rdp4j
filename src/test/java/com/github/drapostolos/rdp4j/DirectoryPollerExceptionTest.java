package com.github.drapostolos.rdp4j;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DirectoryPollerExceptionTest {

    @Test
    public void canContainNoMembers() throws Exception {
        // when 
        DirectoryPollerException ex = new DirectoryPollerException();

        // then
        assertThat(ex.getMessage()).isNull();
        assertThat(ex.getCause()).isNull();
    }

    @Test
    public void canHoldMessageOnly() throws Exception {
        // given 
        String message = "some-message";

        // when 
        DirectoryPollerException ex = new DirectoryPollerException(message);

        // then
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isNull();
    }

    @Test
    public void canHoldCauseOnly() throws Exception {
        // given 
        RuntimeException rt = new RuntimeException();

        // when 
        DirectoryPollerException ex = new DirectoryPollerException(rt);

        // then
        assertThat(ex.getMessage()).isEqualTo(rt.toString());
        assertThat(ex.getCause()).isEqualTo(rt);
    }

    @Test
    public void canHoldCauseAndMessage() throws Exception {
        // given 
        String message = "some-message";
        RuntimeException rt = new RuntimeException();

        // when 
        DirectoryPollerException ex = new DirectoryPollerException(message, rt);

        // then
        assertThat(ex.getMessage()).isEqualTo(message);
        assertThat(ex.getCause()).isEqualTo(rt);
    }

}
