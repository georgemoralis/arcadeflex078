package common;

import static common.ptr.*;

/**
 *
 * @author shadow
 */
public class subArrays {

    public static class UShortArray {

        public UShortArray(int size) {
            memory = new char[size];
            offset = 0;
        }

        public UShortArray(char[] m) {
            set(m, 0);
        }

        public UShortArray(char[] m, int b) {
            set(m, b);
        }

        public UShortArray(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UShortArray(UShortArray cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UShortArray(UShortArray cp) {
            set(cp.memory, cp.offset);
        }

        public char read(int offs) {
            return memory[offs + offset];
        }

        public char read() {
            return memory[offset];
        }

        public void write(int offs, int value) {
            memory[offset + offs] = (char) value;
        }

        public void set(char[] m, int b) {
            memory = m;
            offset = b;
        }

        public char[] memory;
        public int offset;
    }

    public static class IntArray {

        public int[] buffer;
        public int offset;

        public IntArray(int size) {
            this.buffer = new int[size];
            this.offset = 0;
        }

        public IntArray(int[] buffer) {
            this.buffer = buffer;
            this.offset = 0;
        }

        public IntArray(IntArray subarray) {
            this.buffer = subarray.buffer;
            this.offset = subarray.offset;
        }

        public IntArray(IntArray subarray, int offset) {
            this.buffer = subarray.buffer;
            this.offset = subarray.offset + offset;
        }

        public IntArray(int[] buffer, int offset) {
            this.buffer = buffer;
            this.offset = offset;
        }

        public int read() {
            return buffer[offset];
        }

        public int read(int index) {
            return buffer[index + offset];
        }
        
        public int readinc() {
            return buffer[offset++];
        }

        public void write(int value) {
            buffer[offset] = value;
        }

        public void write(int index, int value) {
            buffer[index + offset] = value;
        }

        public void writeinc(int value) {
            buffer[offset++] = value;
        }

        public void writedec(int value) {
            buffer[offset--] = value;
        }

    }
}
