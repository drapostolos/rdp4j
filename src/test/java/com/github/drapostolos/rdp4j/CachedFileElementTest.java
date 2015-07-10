package com.github.drapostolos.rdp4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.github.drapostolos.rdp4j.spi.FileElement;

public class CachedFileElementTest {

    CachedFileElement cached;
    String name = "some-name";
    long lastModified = 1234;
    boolean isDirectory = false;
    FileElement fileElement = new FileElement() {

        @Override
        public long lastModified() throws IOException {
            return lastModified;
        }

        @Override
        public boolean isDirectory() {
            return isDirectory;
        }

        @Override
        public String getName() {
            return name;
        }
    };

    @Before
    public void testName() throws Exception {
        cached = new CachedFileElement(fileElement, name, lastModified);
    }

    @Test
    public void canHoldName() throws Exception {
        assertThat(cached.getName()).isEqualTo(name);
    }

    @Test
    public void canHoldLastModified() throws Exception {
        assertThat(cached.lastModified()).isEqualTo(lastModified);
    }

    @Test
    public void canHoldFileElement() throws Exception {
        assertThat(cached.fileElement).isEqualTo(fileElement);
    }

    @Test
    public void canHoldDirectory() throws Exception {
        isDirectory = true;
        assertThat(cached.isDirectory()).isEqualTo(true);
    }

    @Test
    public void canHoldNoneDirectory() throws Exception {
        assertThat(cached.isDirectory()).isEqualTo(false);
    }

}
