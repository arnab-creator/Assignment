//FIX:

public class ReportDAO {

    private DataSource dataSource;
    
    public List<ReportEntry> fetchMonthlyReport(String accountId,
                                              int month, int year)
                                              throws SQLException {

        // FIX: Connection and PreparedStatement declared in outer TWR.
        //      ResultSet declared in inner TWR after parameters are bound.
        //      This preserves correct closure order: rs → ps → conn.
        try (Connection conn = dataSource.getConnection();                 // FIX: auto-closed
             PreparedStatement ps = conn.prepareStatement(                 // FIX: auto-closed
                 "SELECT * FROM report_entries " +
                 "WHERE account_id = ? AND MONTH(entry_date) = ? " +
                 "AND YEAR(entry_date) = ?"
             )) {

            ps.setString(1, accountId);
            ps.setInt(2, month);
            ps.setInt(3, year);

            // FIX: ResultSet in its own inner TWR so parameters are set first,
            //      then execute, then iterate — correct and safe sequence.
            try (ResultSet rs = ps.executeQuery()) {                       // FIX: auto-closed
                List<ReportEntry> entries = new ArrayList<>();
                while (rs.next()) {
                    entries.add(mapRow(rs));
                }
                return entries;
            }
            // rs closed here
        }
        // ps closed, then conn closed (returned to pool) — even on exception
    }