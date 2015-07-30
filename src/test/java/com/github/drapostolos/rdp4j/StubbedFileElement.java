package com.github.drapostolos.rdp4j;

import java.io.IOException;

import com.github.drapostolos.rdp4j.spi.FileElement;

/*
 * Make a stub so equals/hashCode methods can be implemented correctly.
 */
public class StubbedFileElement implements FileElement {

    private final String name;
    private final long lastModified;

    public StubbedFileElement(String name, long lastModified) {
        this.name = name;
        this.lastModified = lastModified;
    }

    @Override
    public long lastModified() throws IOException {
        return lastModified;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StubbedFileElement other = (StubbedFileElement) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(hashCode()).append(":")
                .append(name).append(":")
                .append(String.valueOf(lastModified))
                .toString();
    }

}
