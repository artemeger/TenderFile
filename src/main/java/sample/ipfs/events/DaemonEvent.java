package sample.ipfs.events;


public class DaemonEvent extends Event {

        private DaemonEvent.DaemonEventType type;

        public DaemonEvent (DaemonEvent.DaemonEventType type) {
            this.type = type;
        }

        public DaemonEvent.DaemonEventType getType() {
            return this.type;
        }

        public static enum DaemonEventType {
            DAEMON_STARTED,
            DAEMON_STOPPED,
            INIT_STARTED,
            INIT_DONE,
            ATTACHED;

            private DaemonEventType() {
            }
        }
}
