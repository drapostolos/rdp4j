package com.github.drapostolos.rdp4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class JavaIoFileAdapterTest {

    private File file = Mockito.mock(File.class);
    private JavaIoFileAdapter adapter = new JavaIoFileAdapter(file);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowWhenConstructorIsGivenNullObject() throws Exception {
        // given
        exception.expect(NullPointerException.class);
        exception.expectMessage("null argument not allowed!");

        // when
        new JavaIoFileAdapter(null);
    }

    @Test
    public void canRetrieveOriginalFileObject() throws Exception {
        assertThat(adapter.getFile()).isSameAs(file);
    }

    @Test
    public void canRedirectToUnderlyingFileObjectWhenCallingMethod_lastModified() throws Exception {
        // given 
        long lastModified = new Date().getTime();
        Mockito.when(file.lastModified()).thenReturn(lastModified);

        // then
        assertThat(adapter.lastModified()).isEqualTo(lastModified);
        Mockito.verify(file).lastModified();
    }

    @Test
    public void shouldThrowIoExceptionWhenLastModifiedReturnsZero() throws Exception {
        // given 
        exception.expect(IOException.class);
        exception.expectMessage("Unknown I/O error occured when retriveing lastModified " +
                "attribute for file '");
        exception.expectMessage(file.toString());

        long lastModified = 0L;
        Mockito.when(file.lastModified()).thenReturn(lastModified);

        // then
        adapter.lastModified();
    }

    @Test
    public void canRedirectToUnderlyingFileObjectWhenCallingMethod_isDirectory() throws Exception {
        // when
        Mockito.when(file.isDirectory()).thenReturn(true);

        //then
        assertThat(adapter.isDirectory()).isTrue();
        Mockito.verify(file).isDirectory();
    }

    @Test
    public void canRedirectToUnderlyingFileObjectWhenCallingMethod_getName() throws Exception {
        // when
        Mockito.when(file.getName()).thenReturn("some-name");

        //then
        assertThat(adapter.getName()).isEqualTo("some-name");
        Mockito.verify(file).getName();
    }

    @Test
    public void canRedirectToUnderlyingFileObjectWhenCallingMethod_toString() throws Exception {
        // when
        Mockito.when(file.toString()).thenReturn("some-string");

        //then
        assertThat(adapter.toString()).isEqualTo("some-string");
    }

    @Test
    public void canRedirectToUnderlyingFileObjectWhenCallingMethod_listFiles() throws Exception {
        // when
        Mockito.when(file.listFiles()).thenReturn(new File[] { file });

        //then
        assertThat(adapter.listFiles()).containsExactly(adapter);
    }

    @Test
    public void shouldThrowIoExceptionWhen_listFiles_methodReturnsNull() throws Exception {
        // given 
        exception.expect(IOException.class);
        exception.expectMessage("Unknown I/O error when listing files in directory '");
        exception.expectMessage(file.toString());

        Mockito.when(file.listFiles()).thenReturn(null);

        // then
        adapter.listFiles();
    }

}
