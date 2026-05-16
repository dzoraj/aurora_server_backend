package rs.igapp.aurora.agent.config;

public enum AgentMode {
    /** smoke test */
    ONCE,
    /** Poll for new lines. */
    FILE,
    /** Read lines from stdin => for quick testing */
    STDIN
}
