package com.lifeos.db;

import org.h2.api.Trigger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class H2SystemLawsTriggers implements Trigger {

    private int colId = -1;
    private int colCurrentXp = -1;
    private int colLevel = -1;
    private int colRank = -1;
    private int colPlayerId = -1;
    private String triggerName;
    
    private int colExamPlayerId = -1;
    private int colExamStatus = -1;

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
        this.triggerName = triggerName;
        try (ResultSet rs = conn.getMetaData().getColumns(null, schemaName, tableName, null)) {
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME").toLowerCase();
                int ordinal = rs.getInt("ORDINAL_POSITION") - 1; 
                
                if (tableName.equalsIgnoreCase("PLAYER_PROGRESSION") || tableName.equalsIgnoreCase("PROJECT")) {
                    if (colName.equals("id")) colId = ordinal;
                    else if (colName.equals("current_xp")) colCurrentXp = ordinal;
                    else if (colName.equals("level")) colLevel = ordinal;
                    else if (colName.equals("rank")) colRank = ordinal;
                    else if (colName.equals("player_id")) colPlayerId = ordinal;
                } else if (tableName.equalsIgnoreCase("RANK_EXAM_ATTEMPTS")) {
                    if (colName.equals("player_id")) colExamPlayerId = ordinal;
                    else if (colName.equals("status")) colExamStatus = ordinal;
                }
            }
        }
    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        if (triggerName == null) return;
        
        if (triggerName.equalsIgnoreCase("TRG_PENALTY_EMBARGO")) {
            checkPenaltyEmbargo(conn, oldRow, newRow);
        } else if (triggerName.equalsIgnoreCase("TRG_RANK_CEILING")) {
            checkRankCeiling(newRow);
        } else if (triggerName.equalsIgnoreCase("TRG_STATE_EXCLUSIVITY")) {
            checkStateExclusivity(conn, newRow);
        }
    }
    
    // ... existing checks ...

    private void checkStateExclusivity(Connection conn, Object[] newRow) throws SQLException {
        if (colExamPlayerId == -1 || colExamStatus == -1) return;

        Object playerIdObj = newRow[colExamPlayerId];
        String status = (String) newRow[colExamStatus];

        // Status check: IN_PROGRESS or UNLOCKED or ACTIVE. 
        // Need to match Enum string values.
        if ("IN_PROGRESS".equals(status) || "UNLOCKED".equals(status) || "ACTIVE".equals(status)) {
             try (PreparedStatement valStmt = conn.prepareStatement(
                    "SELECT 1 FROM player_state_snapshot WHERE player_id = ? AND in_penalty_zone = TRUE")) {
                valStmt.setObject(1, playerIdObj);
                try (ResultSet rs = valStmt.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Law 4 Violation: Cannot start Promotion Exam while in Penalty Zone");
                    }
                }
            }
        }
    }

    private void checkPenaltyEmbargo(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        if (colCurrentXp == -1 || colPlayerId == -1) return; // initialization failed or columns mismatch

        Long oldXp = (Long) oldRow[colCurrentXp];
        Long newXp = (Long) newRow[colCurrentXp];
        Object playerIdObj = newRow[colPlayerId];

        if (newXp > oldXp) {
            try (PreparedStatement valStmt = conn.prepareStatement(
                    "SELECT 1 FROM player_state_snapshot WHERE player_id = ? AND in_penalty_zone = TRUE")) {
                valStmt.setObject(1, playerIdObj);
                try (ResultSet rs = valStmt.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Law 1 Violation: Cannot increase XP while in Penalty Zone");
                    }
                }
            }
        }
    }

    private void checkRankCeiling(Object[] newRow) throws SQLException {
        if (colLevel == -1 || colRank == -1) {
            // If columns not found, maybe table schema differs in test. Log warning or return?
            // For safety, return. Test might fail if trigger logic isn't executed.
            return;
        }

        Integer level = (Integer) newRow[colLevel];
        String rank = (String) newRow[colRank];
        
        int cap = getRankCap(rank);
        if (level > cap) {
            throw new SQLException("Law 2 Violation: Level " + level + " exceeds cap " + cap + " for Rank " + rank);
        }
    }

    private int getRankCap(String rank) {
        if (rank == null) return 10;
        return switch (rank) {
            case "F" -> 10;
            case "E" -> 20;
            case "D" -> 30;
            case "C" -> 40;
            case "B" -> 50;
            case "A" -> 60;
            case "S" -> 70;
            case "SS" -> 80;
            case "SSS" -> 90;
            case "X" -> 100;
            default -> 10;
        };
    }

    @Override
    public void close() throws SQLException {}
    @Override
    public void remove() throws SQLException {}
}
