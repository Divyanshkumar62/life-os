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

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
        this.triggerName = triggerName;
        try (ResultSet rs = conn.getMetaData().getColumns(null, schemaName, tableName, null)) {
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME").toLowerCase();
                int ordinal = rs.getInt("ORDINAL_POSITION") - 1; // 0-based index for row array?
                // H2 Trigger rows are 0-based. MetaData is 1-based?
                // H2 docs: "The object array contains the values ...... index 0 is the first column."
                
                if (colName.equals("id")) colId = ordinal;
                else if (colName.equals("current_xp")) colCurrentXp = ordinal;
                else if (colName.equals("level")) colLevel = ordinal;
                else if (colName.equals("rank")) colRank = ordinal;
                else if (colName.equals("player_id")) colPlayerId = ordinal;
            }
        }
    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        if ("TRG_PENALTY_EMBARGO".equalsIgnoreCase(triggerName)) {
            checkPenaltyEmbargo(conn, oldRow, newRow);
        } else if ("TRG_RANK_CEILING".equalsIgnoreCase(triggerName)) {
            checkRankCeiling(newRow);
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
