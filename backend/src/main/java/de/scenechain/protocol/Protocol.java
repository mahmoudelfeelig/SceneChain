package de.scenechain.protocol;

import java.nio.ByteBuffer;
import java.util.List;

public final class Protocol {
    public static final int PROTOCOL_VERSION = 1;
    public static final int POLICY_VERSION = 1;
    public static final int COLUMNS = 24;
    public static final int ROWS = 16;
    public static final int STAGES = 5;
    public static final int ACTIONS = 4;
    public static final int MARKERS = 8;

    private Protocol() {}

    public record Stage(int sceneId, int cellId, int actionId) {
        public Stage {
            if (sceneId <= 0) throw new IllegalArgumentException("Invalid scene");
            if (cellId < 0 || cellId >= COLUMNS * ROWS) throw new IllegalArgumentException("Invalid cell");
            if (actionId < 0 || actionId >= ACTIONS) throw new IllegalArgumentException("Invalid action");
        }
    }

    public static int quantize(int x16, int y16) {
        if (x16 < 0 || x16 > 65535 || y16 < 0 || y16 > 65535) {
            throw new IllegalArgumentException("Coordinate outside unsigned 16-bit range");
        }
        int column = (int) (((long) x16 * COLUMNS) / 65536L);
        int row = (int) (((long) y16 * ROWS) / 65536L);
        return row * COLUMNS + column;
    }

    public static byte[] encode(List<Stage> stages) {
        if (stages.size() != STAGES) throw new IllegalArgumentException("Exactly five stages required");
        ByteBuffer out = ByteBuffer.allocate(4 + STAGES * 7);
        out.put((byte) PROTOCOL_VERSION);
        out.putShort((short) POLICY_VERSION);
        out.put((byte) STAGES);
        for (Stage stage : stages) {
            out.putInt(stage.sceneId());
            out.putShort((short) stage.cellId());
            out.put((byte) stage.actionId());
        }
        return out.array();
    }

    public static List<Stage> decode(byte[] encoded) {
        if (encoded.length != 4 + STAGES * 7) throw new IllegalArgumentException("Invalid credential length");
        ByteBuffer in = ByteBuffer.wrap(encoded);
        int version = Byte.toUnsignedInt(in.get());
        int policy = Short.toUnsignedInt(in.getShort());
        int count = Byte.toUnsignedInt(in.get());
        if (version != PROTOCOL_VERSION || policy != POLICY_VERSION || count != STAGES) {
            throw new IllegalArgumentException("Unsupported credential header");
        }
        var stages = new java.util.ArrayList<Stage>(STAGES);
        for (int i = 0; i < STAGES; i++) {
            stages.add(new Stage(in.getInt(), Short.toUnsignedInt(in.getShort()), Byte.toUnsignedInt(in.get())));
        }
        return List.copyOf(stages);
    }

    public static int challengeTile(int cellId) {
        int row = cellId / COLUMNS;
        int col = cellId % COLUMNS;
        return (row / 2) * 12 + (col / 2);
    }
}
