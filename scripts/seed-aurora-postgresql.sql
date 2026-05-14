
-- Aurora SIEM -- PostgreSQL seed data

BEGIN;

TRUNCATE TABLE incident_alerts RESTART IDENTITY CASCADE;
TRUNCATE TABLE alerts RESTART IDENTITY CASCADE;
TRUNCATE TABLE incidents RESTART IDENTITY CASCADE;
TRUNCATE TABLE log_events RESTART IDENTITY CASCADE;
TRUNCATE TABLE rules RESTART IDENTITY CASCADE;
TRUNCATE TABLE sources RESTART IDENTITY CASCADE;
TRUNCATE TABLE rule_statuses RESTART IDENTITY CASCADE;
TRUNCATE TABLE alert_statuses RESTART IDENTITY CASCADE;
TRUNCATE TABLE severities RESTART IDENTITY CASCADE;


INSERT INTO rule_statuses (id, name, description, is_deleted, deleted_at)
VALUES
  (1, 'ACTIVE',   'Rule is evaluated by the detection engine', false, NULL),
  (2, 'INACTIVE', 'Rule exists but is not evaluated',            false, NULL),
  (3, 'ARCHIVED', 'Retired rule kept for audit',                  false, NULL);


INSERT INTO severities (id, name, level, description, is_deleted, deleted_at)
VALUES
  (1, 'INFO',     1, 'Informational',        false, NULL),
  (2, 'LOW',      2, 'Low priority',         false, NULL),
  (3, 'MEDIUM',   3, 'Medium priority',      false, NULL),
  (4, 'HIGH',     4, 'High priority',        false, NULL),
  (5, 'CRITICAL', 5, 'Critical / immediate', false, NULL);


INSERT INTO alert_statuses (id, name, description, is_deleted, deleted_at)
VALUES
  (1, 'NEW',             'New alert / incident not triaged', false, NULL),
  (2, 'INVESTIGATING',   'Under active investigation',     false, NULL),
  (3, 'RESOLVED',        'Closed as legitimate / fixed',   false, NULL),
  (4, 'FALSE_POSITIVE',  'Benign or incorrect detection',  false, NULL);


INSERT INTO sources (
  id, agent_id, hostname, ip_address, os_type, agent_version, is_active,
  last_heartbeat, created_at, updated_at, is_deleted, deleted_at
) VALUES
  (
    1,
    'agent-win-desk-01',
    'DESKTOP-IGOR',
    '192.168.1.10',
    'Windows',
    '1.0.0',
    true,
    NOW() - INTERVAL '2 minutes',
    NOW() - INTERVAL '7 days',
    NOW() - INTERVAL '2 minutes',
    false,
    NULL
  ),
  (
    2,
    'agent-linux-srv-01',
    'web-01.prod',
    '10.0.0.22',
    'Linux',
    '1.0.0',
    true,
    NOW() - INTERVAL '5 minutes',
    NOW() - INTERVAL '14 days',
    NOW() - INTERVAL '5 minutes',
    false,
    NULL
  );


INSERT INTO rules (
  id, name, description, condition, status_id, severity_id, enabled, alert_message,
  created_at, updated_at, is_deleted, deleted_at
) VALUES
  (
    1,
    'Failed Windows login burst',
    'Correlate multiple failed logon events for the same account.',
    'message CONTAINS ''Failed logon'' OR message CONTAINS ''4625''',
    1,
    4,
    true,
    'Possible brute-force: {message}',
    NOW() - INTERVAL '30 days',
    NOW() - INTERVAL '1 day',
    false,
    NULL
  ),
  (
    2,
    'Suspicious PowerShell encoded command',
    'Detects long Base64-like command lines in process creation.',
    'message ~ ''(?i)-enc.*[A-Za-z0-9+/]{80,}''',
    1,
    5,
    true,
    'Encoded PowerShell execution on {hostname}: {message}',
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '2 days',
    false,
    NULL
  ),
  (
    3,
    'Linux sudo escalation (disabled demo)',
    'Placeholder rule kept inactive for UI testing.',
    'message CONTAINS ''sudo:''',
    2,
    3,
    false,
    'Sudo activity: {message}',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '10 days',
    false,
    NULL
  );


INSERT INTO log_events (
  id, source_id, message, severity_id, raw_data, timestamp, created_at
) VALUES
  (
    1,
    1,
    'Audit Failure: An account failed to log on. Subject: admin Target: DESKTOP-IGOR',
    4,
    '{"event_id":4625,"channel":"Security"}'::jsonb,
    NOW() - INTERVAL '3 hours',
    NOW() - INTERVAL '3 hours'
  ),
  (
    2,
    1,
    'Audit Failure: An account failed to log on. Subject: admin Target: DESKTOP-IGOR',
    4,
    '{"event_id":4625,"channel":"Security"}'::jsonb,
    NOW() - INTERVAL '2 hours 55 minutes',
    NOW() - INTERVAL '2 hours 55 minutes'
  ),
  (
    3,
    2,
    'sshd: Failed password for invalid user oracle from 203.0.113.50 port 49152 ssh2',
    4,
    '{"daemon":"sshd","src_ip":"203.0.113.50"}'::jsonb,
    NOW() - INTERVAL '40 minutes',
    NOW() - INTERVAL '40 minutes'
  ),
  (
    4,
    1,
    'powershell.exe -NoP -W Hidden -Enc JABzAD0ATgBlAHcALQBPAGIAagBlAGMAdAAgAEkATwAuAE0AZQBtAG8AcgB5AFMAdAByAGUAYQBtAA==',
    5,
    '{"parent":"explorer.exe"}'::jsonb,
    NOW() - INTERVAL '15 minutes',
    NOW() - INTERVAL '15 minutes'
  );

INSERT INTO alerts (
  id, rule_id, log_event_id, source_id, severity_id, status_id,
  message, assigned_to, investigation_notes, resolved_at,
  created_at, updated_at, is_deleted, deleted_at
) VALUES
  (
    1,
    1,
    1,
    1,
    4,
    2,
    'Multiple failed logons for admin from DESKTOP-IGOR within a short window.',
    'analyst-igor',
    'Confirmed lab subnet; correlating with VPN logs.',
    NULL,
    NOW() - INTERVAL '2 hours 50 minutes',
    NOW() - INTERVAL '30 minutes',
    false,
    NULL
  ),
  (
    2,
    1,
    2,
    1,
    3,
    1,
    'Single failed logon for admin (below threshold).',
    NULL,
    NULL,
    NULL,
    NOW() - INTERVAL '2 hours',
    NOW() - INTERVAL '2 hours',
    false,
    NULL
  ),
  (
    3,
    2,
    4,
    1,
    5,
    1,
    'Encoded PowerShell command line observed on workstation.',
    NULL,
    NULL,
    NULL,
    NOW() - INTERVAL '14 minutes',
    NOW() - INTERVAL '14 minutes',
    false,
    NULL
  ),
  (
    4,
    1,
    3,
    2,
    4,
    3,
    'SSH brute-force pattern from 203.0.113.50 against web-01.prod.',
    'analyst-igor',
    'Blocked IP at perimeter; closing.',
    NOW() - INTERVAL '10 minutes',
    NOW() - INTERVAL '35 minutes',
    NOW() - INTERVAL '10 minutes',
    false,
    NULL
  );

INSERT INTO incidents (
  id, title, description, severity_id, status_id, assigned_to, timeline,
  resolved_at, created_at, updated_at
) VALUES
  (
    1,
    'Brute-force against admin on DESKTOP-IGOR',
    'Grouped failed logon alerts from a single workstation.',
    4,
    2,
    'analyst-igor',
    '09:00 Triage started\n09:20 Confirmed internal test account; not malicious.',
    NULL,
    NOW() - INTERVAL '2 hours 45 minutes',
    NOW() - INTERVAL '25 minutes'
  ),
  (
    2,
    'SSH noise from 203.0.113.50',
    'Internet scanner hitting public SSH; tracked in perimeter SIEM.',
    3,
    3,
    'analyst-igor',
    'IP blocked; no customer impact.',
    NOW() - INTERVAL '10 minutes',
    NOW() - INTERVAL '36 minutes',
    NOW() - INTERVAL '10 minutes'
  );

INSERT INTO incident_alerts (incident_id, alert_id) VALUES
  (1, 1),
  (1, 2),
  (2, 4);


SELECT setval(pg_get_serial_sequence('rule_statuses', 'id'),  (SELECT COALESCE(MAX(id), 1) FROM rule_statuses),  true);
SELECT setval(pg_get_serial_sequence('severities', 'id'),    (SELECT COALESCE(MAX(id), 1) FROM severities),    true);
SELECT setval(pg_get_serial_sequence('alert_statuses', 'id'), (SELECT COALESCE(MAX(id), 1) FROM alert_statuses), true);
SELECT setval(pg_get_serial_sequence('sources', 'id'),        (SELECT COALESCE(MAX(id), 1) FROM sources),        true);
SELECT setval(pg_get_serial_sequence('rules', 'id'),          (SELECT COALESCE(MAX(id), 1) FROM rules),          true);
SELECT setval(pg_get_serial_sequence('log_events', 'id'),     (SELECT COALESCE(MAX(id), 1) FROM log_events),     true);
SELECT setval(pg_get_serial_sequence('alerts', 'id'),         (SELECT COALESCE(MAX(id), 1) FROM alerts),         true);
SELECT setval(pg_get_serial_sequence('incidents', 'id'),      (SELECT COALESCE(MAX(id), 1) FROM incidents),      true);

COMMIT;
