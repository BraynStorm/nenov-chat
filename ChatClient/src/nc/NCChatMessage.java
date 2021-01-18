package nc;

public abstract class NCChatMessage {
    
    public abstract long getSender();

    public abstract String getContent();

    public static class Direct extends NCChatMessage {
        private long sender;
        private long receiver;
        private String content;

        public Direct(long sender, long receiver, String content) {
            this.sender = sender;
            this.receiver = receiver;
            this.content = content;
        }

        @Override
        public long getSender() {
            return sender;
        }

        @Override
        public String getContent() {
            return content;
        }
    }
}
