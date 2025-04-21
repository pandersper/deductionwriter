package control.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

import control.Session;
import control.Shortcut;
import control.Toolbox;
import control.WorkList;
import model.description.DComposite;
import model.description.DPrimitive;
import model.description.DStatement;
import model.description.DTheorem;
import model.description.abstraction.Described;
import model.independent.DoubleArray;
import model.independent.DoubleArray.Tuple;
import model.logic.Implication.ImplicationType;
import model.logic.abstraction.Formal;
import view.components.DisplayCanvas;

/**
 * Front end for the storage of theorems and all their parts. Back end is sqlite <a href="http://www.sqlite.org">www.sqlite.org</a>.<br><br>
 *
 * A lot of functionality of the application has been chosen to rather reside in the base, as views, instead of
 * rebuilding them at start-up and keeping collections in runtime objects. The currently used keyboard
 * bindings are cached in that way for example.
 */
public class DeductionBase {
	

	private Connection connection;

	private boolean passivemode;
	
	/**
	 * Instantiates a new data base front-end and back-end where the base file is located at 'data/sqlite/variables.sqlite'. 
	 * The mini database is run by sqlite. <a href="www.sqlite.org">www.sqlite.org</a>.
	 *
	 * @param passivemode If this class should be run without the back-end database. For debugging purposes.
	 */
	public DeductionBase(boolean passivemode) {

		this.passivemode = passivemode;

		if (!passivemode) {

			Stream<Driver> drivers = DriverManager.drivers();
			
			for (Driver d : drivers.toList()) 
				if (Toolbox.DEBUGMINIMAL) System.out.println("Driver:\t\t\t" + d.toString());

			try {
				connection = DriverManager.getConnection("jdbc:sqlite:data/sqlite/variables.sqlite");
				
				if (Toolbox.DEBUGMINIMAL) StaticSQL.connectionInfo(connection);
								
				this.setupDB();
			} 
			catch (Exception ioe) { ioe.printStackTrace(); }	
		}
	}																																
	
	/* * * * * * * * * * * * * database setup * * * * * * * * * * * * * */
	
	/**
	 * Drops all tables in the database.
	 */
	public void dropDB() {

		try (Statement s = connection.createStatement()) {

			///(5992)
			s.addBatch("DROP TABLE IF EXISTS Composites;");
			s.addBatch("DROP TABLE IF EXISTS Statements;");
			s.addBatch("DROP TABLE IF EXISTS Theorems;");
			s.addBatch("DROP TABLE IF EXISTS Sessions;");

			endTransaction(s, "Main tables dropped! ");

			s.close();
			
			dropViews();

		} catch (SQLException e) { e.printStackTrace(); }
	}

	/**
	 * Drops all constructed views.
	 *
	 * @throws SQLException 
	 */
	private void dropViews() 		throws SQLException {

		try (Statement s = connection.createStatement()) {

			///(F138)
			s.addBatch("DROP VIEW IF EXISTS Theoremnames;");

			endTransaction(s, "Obligatory views dropped! ");

			s.close();
		}
	}
	
	/**
	 * Drops all views constructed by users of the program.
	 *
	 * @throws SQLException
	 */
	private void dropUserViews() 	throws SQLException {

		try (Statement s = connection.createStatement()) {
			s.execute("SELECT glyphtablename FROM Primitivetables;");		

			ResultSet result = s.getResultSet();

			String sql = "DROP VIEW IF EXISTS ";

			while (result.next()) {

				sql += result.getString(1) + ";";

				s.addBatch(sql);

				sql = "DROP VIEW IF EXISTS ";		
			}

			s.executeBatch();

			this.endTransaction(s, "User views dropped");

			s.addBatch("DROP VIEW IF EXISTS Primitivetables;");

			this.endTransaction(s, "User views register dropped");
		}
	}
	
	/**
	 * Sets up all tables and views necessary to use the application at length.
	 *
	 * @throws SQLException		General SQL exception for now.
	 */
	public void setupDB() 			throws SQLException {

		try (Statement s = connection.createStatement()) {

			s.addBatch(StaticSQL.sessions_setup);
			s.addBatch(StaticSQL.theorems_setup);
			s.addBatch(StaticSQL.statements_setup);
			s.addBatch(StaticSQL.primitives_setup);
			s.addBatch(StaticSQL.composites_setup);
			s.addBatch(StaticSQL.theoremnames_view);
			s.addBatch(StaticSQL.primitivestables_view);
			s.addBatch(StaticSQL.compositestables_view);

			endTransaction(s, "DB tables are set up ok! ");
		}		
	}	
	
	/**
	 * Closes down the back-end connection. Does not exit.
	 */
	public void closeDB() {

		if (!passivemode) 
			try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }		
	}

	/* * * * * * * * * * * * database conditionals  * * * * * * * * * * */	
	
	public boolean 		isConsistent(Session session) {

		try {

			LinkedList<DTheorem> theorems = new LinkedList<DTheorem>();		
			LinkedList<String> theoremsnames = new LinkedList<String>();

			DTheorem theorem;
			int statements = 0;
			
			for (DisplayCanvas canvas : session.getWorks()) {
				
				theorem = canvas.getTheorem();
				
				theorems.addLast(theorem);
				theoremsnames.addLast(theorem.getName());

				statements += theorem.lengthInStatements();
			}
			
			String sql= "SELECT theorems FROM Sessions WHERE name='" + session.getName() + "';"; 

			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery(sql);

			String namesstring = "";
			
			if (r.next()) namesstring = r.getString(0);
			else 
				Toolbox.output("Inconsistent db: no names string.",null);

			String[] names = namesstring.split(":");

			if (names.length == theoremsnames.size()) {}
			else { 
			
				Toolbox.output("Inconsistent db: not all sessions theorems in db. Trying to update.",null);
			
				if (this.saveSession(session, " (repair update) ")) Toolbox.output("db: Repair update ok'd.",null);
				else
					Toolbox.output("db: Repair update failed.",null);
			}
			
			DTheorem dbtheorem;
			int dbstatements = 0;
 
			for (DTheorem t : theorems) {
				
				dbtheorem = fetchTheorem(t.getName());
								
				int diff = dbtheorem.lengthInFormals() - t.lengthInFormals();
				
				if (dbtheorem.lengthInFormals() == t.lengthInFormals()) 
					Toolbox.output("db _seems_ ok.",null); 
				else {
					
					Toolbox.output("Inconsistent db: theorem " + t.getName() + " is incorrect in db." + diff + 
																 " primitives differ. Trying repair update.",null); 
					int insertions = this.update(t);
					
					if (insertions == t.lengthInStatements()) Toolbox.output("db: repair uppdate of " + t.getName() + " _seems_ to have worked.",null);
					else	
						Toolbox.output("db: repair uppdate of " + t.getName() + " failed.",null); 
				}
				
				dbtheorem = fetchTheorem(t.getName());		
				dbstatements += dbtheorem.lengthInStatements();
			}
		
			s.closeOnCompletion();		
			
			if (statements == dbstatements) {
				Toolbox.output("db is ok regarding size.",null);
				return true;
			} else {
				Toolbox.output("db is wrong regarding size for the session " + session.getName() + ".",null);
				return false;
			}

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return false;
	}

	/**
	 * Checks if an element exists in a column of a table in the data base.
	 *
	 * @param element 	The element value (string) to search for in the column.
	 * @param table 	The table name.
	 * @param column 	The column name that eventually contains the element name.
	 * 
	 * @return			 Wether the element was found.
	 */
	public boolean 		contains(String element, String table, String column) {

		try (Statement s = connection.createStatement()) {

			s.execute("SELECT COUNT(" + column + ") FROM " + table + " WHERE " + column + "='" +  element + "';");

			ResultSet result = s.getResultSet();

			int occurrences = result.getInt(1);

			s.closeOnCompletion();

			return occurrences > 0;

		} catch (SQLException e) {

			e.printStackTrace();
		}	
		return false;
	}

	/* * * * * * * * * * * * database altering  * * * * * * * * * * * * */	

	public boolean 		saveSession(Session session, String description) {
		
		try {
			LinkedList<DTheorem> theorems = session.getTheorems();		
			String theoremsnames = session.getTheoremsNames();
									
			String sql = StaticSQL.sessions_insert + "('" + session.getName() + "', '"+ theoremsnames + "', '"+ description + "')"; 

			Statement s = connection.createStatement();

			boolean ok = s.execute(sql);

			boolean overwrite = true;

			int insertions = 0;
			
			for (DTheorem theorem : theorems) 
				insertions += this.insert(theorem, overwrite, session.primitivestable, session.compositestable);

			Toolbox.output("saved session: " + session.getName() + " with " + insertions + " insertions. (session ok: " + ok + ")",null);
			
			s.closeOnCompletion();		

			return true;

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return false;
	}
		
	/**
	 * Inserts a new theorem into the base. Also here, no other complementary method should be used. 
	 *
	 * @param theorem 		The theorem to insert.
	 * @param overwrite 	Overwrite if theorem with same name exist.
	 * @return 				The number of sql instructions executed (inserts). 
	 */
	public int 			insert(DTheorem theorem, boolean overwrite, String primitivestable,String compositestable) {
									// Composites then?
		try {
			String[] sqls;

			if (overwrite) return update(theorem);
			else {

				sqls = StaticSQL.TheoremToSQL(theorem);

				Statement s = connection.createStatement();

				for (String sql : sqls) s.addBatch(sql);

				boolean ok = endTransaction(s, "Trying to insert " + sqls.length + " statements.");

				s.clearBatch();

				if (ok) {
					String statementids = statementIDs(theorem);

					s.addBatch(StaticSQL.theorems_insert + "('" + theorem.getName() + "', '" + statementids +  "');");

					ok = endTransaction(s, "Trying to insert a theorem.");

					if (ok) Toolbox.output("All ok. New theorem added to database.",null);
					else Toolbox.output("Something wrong. No new theorem in database.",null);		
				} else Toolbox.output("Something wrong. statements not all in database.",null);		

				s.closeOnCompletion();		
			}
			return sqls.length;

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return -1;
	}

	/**
	 * Updates the data base regarding the theorem given as parameter, and everything related to it.
	 * This is the only method that is needed when updating a theorem. 
	 *
	 * @param theorem 	The theorem which shoud be updated in the base.
	 * @return 			The number of sql instructions executed (updates).
	 */
	public int	 		update(DTheorem theorem) {

		String sql = "SELECT * FROM Statements WHERE id='" + theorem.getName() + "';";

		try (Statement query = connection.createStatement()) {

			query.executeQuery(sql);

			String[] sqls = new String[theorem.lengthInStatements()];

			sqls[0] = sql;

			int i = 1;

			for (DStatement s : theorem) {

				String sequence = Toolbox.parseToString(s);

				sql  = "UPDATE Statements SET sequence='" + sequence + "' ";
				sql += "WHERE id='" + s.getID() + "'";

				sqls[i++] = sql;

				int updated = query.executeUpdate(sql);

				try (ResultSet result = query.getResultSet()) {
					
					if (updated == 1)
						if (result.rowUpdated())
							Toolbox.output("One row updated.",null);
				}
			}			
			return sqls.length;

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return -1;
	}

	/**
	 * Delete an element from the data base.
	 * 
	 * @param table		The table to dele from.
	 * @param column	The tables column name.
	 * @param element	The column value.
	 * @return 			True if deletion carried out.
	 */
	public boolean		delete(String table, String column, String element) {

		try (Statement s = connection.createStatement()) {

			s.execute("DELETE FROM " + table + " WHERE " + column + "='" + element + "';");

			int occurrences = s.getUpdateCount();		// delete is an update-operation in sql, no result set.

			s.closeOnCompletion();

			return occurrences > 0;

		} catch (SQLException e) { e.printStackTrace(); }	

		return false;
	}

	
	/**
	 * Inserts a collection of primitives into the base's table of primitives.
	 *
	 * @param primitives 	The formals to insert.
	 * @param glyphtablename 	The name of the view to which they belong.
	 * @return 				The number of sql commands executed (inserts). 
	 */
	public int			insertPrimitivesTable(Collection<Formal> primitives, String tablename)	{

		String[] sqls = new String[primitives.size()];

		try {

			Statement s = connection.createStatement();

			int i = 0;

			String sql = StaticSQL.primitives_insert;

			for (Formal primitive : primitives) {

				sql += StaticSQL.valuesString(primitive, tablename);

				s.addBatch(sql);

				sqls[i++] = sql;

				sql = StaticSQL.primitives_insert;
			}

			boolean ok = endTransaction(s, "Trying to insert " + primitives.size() + " primitives.");	

			if (ok) Toolbox.output("All ok. Table " + tablename + "View of primitives is in database.",null);
			else Toolbox.output("Something wrong. Check table of primitives, if it exists.",null);

			s.closeOnCompletion();		

			return sqls.length;

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return -1;
	}
	
	/**
	 * Insert a collection of composites into the base's table of composites.
	 *
	 * @param composites 	The composites to insert.
	 * @param prefix 		The name of the view to which they belong.
	 * @return  			The number of sql commands executed (inserts).
	 */
	public int			insertCompositesTable(Collection<DComposite> composites, String prefix)	{

		String[] sqls = new String[composites.size()];

		try {

			Statement s = connection.createStatement();

			int i = 0;

			String sql = StaticSQL.composites_insert;

			for (DComposite composite : composites) {
				
				sql += StaticSQL.valuesString(composite, prefix);

				s.addBatch(sql);

				sqls[i++] = sql;

				sql = StaticSQL.composites_insert;
			}

			boolean ok = endTransaction(s, "Trying to insert " + composites.size() + " composites.");	

			if (ok) Toolbox.output("All ok. Table " + prefix + "View of composites is in database.",null);
			else Toolbox.output("Something wrong. Check table of composites, if it exists.",null);

			s.closeOnCompletion();		

			return sqls.length;

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return -1;
	}
		
	/**
	 * Insert bindings into the database. The bindings are used to enable direct typing of primitives into the theorem.
	 *
	 * @param bijection 	A bijective array of pairs of formals to keyboard key short-cuts. Used to enable typing primitives. 
	 * @param theorem 		The name of the grouping, the view that the formals bound belong to.
	 * @return				The	number of sql commands executed (updates).
	 */
	public int 			insertBindings(DoubleArray<Formal, Shortcut> bijection, DisplayCanvas canvas,String primitivestable, String compositestable)  {

		int counter = 0;

		System.out.println("Not implemented in DeductionBase");
		String primitivesview = primitivestable;
		String compositesview = compositestable;
		
		DoubleArray<Formal, Shortcut> primitives = Toolbox.filterBindings(bijection, Formal.FormalType.VARIABLE);
		DoubleArray<Formal, Shortcut> composites = Toolbox.filterBindings(bijection, Formal.FormalType.COMPOSITE);
				
		try {

			Statement s = connection.createStatement();

			String sqlpre, sqlmid, sql;
			
			if (primitives != null) {

				sqlpre  = "UPDATE Primitives SET ";
				sqlmid = " WHERE glyphtablename='" + primitivesview + "' AND codepoint=";

				for (Tuple<Formal, Shortcut> pair : primitives) {

					Formal p = pair.first();

					sql = sqlpre + "keycode='" + pair.second().keycode + "', " + 
							"modifiers='" + pair.second().modifiers + "'" + 
							sqlmid + p.getCodepoint() + ";";

					s.addBatch(sql);

					counter++;
				}
			}

			if (composites != null) {

				sqlpre  = "UPDATE Composites SET ";
				sqlmid = " WHERE glyphtablename='" + compositesview + "' AND codepoint=";

				for (Tuple<Formal, Shortcut> pair : composites) {

					Formal p = pair.first();

					sql = sqlpre + "keycode='" + pair.second().keycode + "', " + 
							"modifiers='" + pair.second().modifiers + "'" + 
							sqlmid + p.getCodepoint() + ";";

					s.addBatch(sql);

					counter++;
				}
			}
			endTransaction(s,"Inserted shortcuts in Primitives and Composites.");

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return counter;
	}

	
	/**
	 * Creates a new view for managing formals.
	 *
	 * @param viewprefix 	The name prefixed to identify the view.
	 * @param formalsview 	The name of the view to fetch formals data from.
	 * @return 				The number of rows in the new view.
	 */
	public int 			createView(String viewprefix, String formalsview) {

		try {
			
			Statement s = connection.createStatement();

			String sql = "CREATE VIEW IF NOT EXISTS " + viewprefix + "View" + 
					" AS " +
					" SELECT glyphtablename, codepoint, keycode, modifiers " +
					" FROM " + formalsview + " WHERE glyphtablename='" + viewprefix + "'";
			
			s.execute(sql);

			connection.commit();

			int count = 0;

			sql = "SELECT COUNT(codepoint) FROM " + viewprefix + "View;";

			s.execute(sql);

			ResultSet result = s.getResultSet();

			count = result.getInt(1);

			return count;

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return -1;
	}

	/* * * * * * * * * * * * * * * retreivers * * * * * * * * * * * * * * */		

	/**
	 * Fetches sessions worklist from the data base.
	 *
	 * @param sessionname 	The name of the session to fetch worklist for.
	 * @return 				The {@see WorkList} of canvases with theorem for a session.
	 * 
	 * @see WorkList
	 */
	public WorkList 						fetchWorklist(String sessionname) {

		String sql = "SELECT * FROM Sessions WHERE name='" + sessionname + "';";

		try {

			Statement s = connection.createStatement();

			ResultSet r = s.executeQuery(sql);

			String namesstring  	= r.getString(0);
			String primitivestable  = r.getString(1);
			String compositestable  = r.getString(2);
			String description 		= r.getString(3);

			String[] names = namesstring.split(":");

			ArrayList<DisplayCanvas> canvases 	= new ArrayList<DisplayCanvas>();

			for (String name : names) 
				canvases.add(new DisplayCanvas(this.fetchTheorem(name)));

			DoubleArray<Formal, Shortcut> primitives = fetchPrimitives(primitivestable);

			DoubleArray<Described, Shortcut> composites = fetchComposites(compositestable);

			return new WorkList(canvases,primitives,composites,description);

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return null;
	}
	
	/**
	 * Fetches a theorem from the data base.
	 *
	 * @param theoremname 	The name of the theorem.
	 * @return 				The described theorem. Remark: the theorem is not layed out yet. It is layed out over and by a canvas.
	 * 
	 * @see view.components.DisplayCanvas
	 */
	public DTheorem							fetchTheorem(String theoremname) {

		String sql = "SELECT statements FROM Theorems WHERE name='" + theoremname + "';";

		try {

			Statement s = connection.createStatement();

			ResultSet r = s.executeQuery(sql);

			String statements = r.getString(1);

			String[] ids = statements.split(":");

			ArrayList<String> 	sequences 	 = new ArrayList<String>();
			ArrayList<Integer> 	implications = new ArrayList<Integer>();

			for (String id : ids) {

				sql = "SELECT sequence, implication FROM Statements WHERE id='" + id + "';";				

				r = s.executeQuery(sql);

				if (r.next()) {

					sequences.add(r.getString(1));
					implications.add(r.getInt(2));

				} else {

					System.err.println("Base inconsistent.");

					throw new SQLException("Statement with id " + id + " from theorem " + theoremname + " cant't be found i base.");
				}
			}

			ArrayList<ImplicationType> types = StaticSQL.parseToImplicationType(implications);

			ArrayList<ArrayList<Described>> statementlists = new ArrayList<ArrayList<Described>>();

			for (String sequence : sequences) {

				Toolbox.output(null,"Parsing from sequence: " + sequence);

				String[] tokens = sequence.split(":");

				ArrayList<Described> statementlist = new ArrayList<Described>();

				int codepoint;

				for (String token : tokens) {

					if (token.length() > 1) {

						codepoint = Integer.parseInt(token);

						sql = "SELECT codepoint, codepoints, baselines FROM Composites WHERE codepoint='" + codepoint 	+ "';";

						r = s.executeQuery(sql);

						codepoint = r.getInt(1);

						String codepointsstring = r.getString(2);
						String baselinesstring  = r.getString(3);

						DComposite described = new DComposite(DComposite.parseComponents(codepointsstring, baselinesstring), codepoint);

						statementlist.add(described);

					} else {

						codepoint = (int) token.charAt(0);

						Described primitive = new DPrimitive(codepoint);

						statementlist.add(primitive);
					}					
				}

				statementlists.add(statementlist);

				Toolbox.output(null," to " + statementlist.size() + " sized statement.\n");
			}

			DTheorem theorem = new DTheorem(theoremname, statementlists, types);
			
			Toolbox.output("Theorem from db:  #s=" + theorem.lengthInStatements() + ", #p=" + theorem.lengthInFormals(),null);	

			return theorem;

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return null;
	}

	/**
	 * Fetches a mapping of primitives from the data base.
	 *
	 * @param viewname 	The name of the collection (sql view) to fetch. 
	 * @return 			A bijective array of pairs of <b>primitive <u>non-described</u> formals</b> and keyboard key short-cuts. Used to enable typing primitives. 
	 */
	public DoubleArray<Formal, Shortcut> 	fetchPrimitives(String viewname) {			

		DoubleArray<Formal, Shortcut> bindings = new DoubleArray<Formal, Shortcut>();

		try {

			String sql = "SELECT * FROM Primitives WHERE glyphtablename='" + viewname + "';";

			Statement query = connection.createStatement();

			ResultSet result = query.executeQuery(sql);

			while (result.next()) {

				int codepoint = result.getInt(2);
				int keycode = result.getInt(3);
				int modifiers = result.getInt(4);

				DPrimitive primitive = new DPrimitive(codepoint);

				Shortcut binding = null;
				
				if (keycode != -1)
					binding = new Shortcut(keycode, modifiers);

				bindings.add(new Tuple<Formal, Shortcut>(primitive.value(), binding));
			}

		} catch (SQLException e) { e.printStackTrace(); }

		return bindings;
	}

	/**
	 * Fetches a mapping of composites from the data base.
	 *
	 * @param viewname 	The name of the collection (sql view) to fetch. 
	 * @return 			A bijective array of pairs of <b><u>described</u> composite formals</b> and keyboard key short-cuts. Used to enable typing primitives. 
	 */	
	public DoubleArray<Described, Shortcut> fetchComposites(String viewname) {

		DoubleArray<Described, Shortcut> bindings = new DoubleArray<Described, Shortcut>();

		try {

			String sql = "SELECT * FROM Composites where glyphtablename='" + viewname + "';";

			Statement query = connection.createStatement();

			ResultSet result = query.executeQuery(sql);

			while (result.next()) {

				int codepoint 		= result.getInt(2);
				String codepoints 	= result.getString(3);
				String baselines 	= result.getString(4);				
				int keycode 		= result.getInt(5);
				int modifiers 		= result.getInt(6);

				DComposite composite = new DComposite(DComposite.parseComponents(codepoints, baselines), codepoint);			

				Shortcut binding = null; 
				
				if (keycode != -1)
					binding = new Shortcut(keycode, modifiers);

				bindings.add(new Tuple<Described, Shortcut>(composite, binding));
			}

		} catch (SQLException e) { e.printStackTrace(); }

		return bindings;
	}

	/**
	 * Fetch theorem names.
	 *
	 * @return The names of all theorems stored.
	 */
	public ArrayList<String> 				fetchTheoremNames()  {

		return fetchStringColumn("name", "Theorems");
	}

	/**
	 * BAD IMPLEMENTATION - BE MORE SPECIFIC AND HANDLE ERRORS.
	 * 
	 * Fetches everything from a table.
	 *
	 * @param glyphtablename The name of the tabe to select from.
	 * @return One array of strings.
	 */
 	public ArrayList<String> 				fetchNames(String tablename) {

		return fetchStringColumn("*", tablename);

	}

 	/**
 	 * Fetch a category of primitives belonging to view with the given cetagoryname.
 	 * 
 	 * @param categoryname	The name of the category in the primitives table.
 	 * @return				An array of codepoints belonging to the category.
 	 */
	public ArrayList<Integer> 				fetchCategory(String categoryname) {

		ArrayList<Integer> codepoints = new ArrayList<Integer>();

		String sql = "SELECT codepoint FROM Primitives WHERE glyphtablename='" + categoryname + "';";

		try {

			Statement s = connection.createStatement();

			ResultSet result = s.executeQuery(sql);

			while (result.next()) 			
				codepoints.add(result.getInt(1));

		} catch (SQLException sqle) { sqle.printStackTrace(); }
		
		return codepoints;
	}
	
	/* * * * * * * * * * * * helpers, not alternating * * * * * * * * * * * */
		
	private boolean 			endTransaction(Statement s, String message) 	throws SQLException  {

		int[] oks = s.executeBatch();

		boolean allok = StaticSQL.allok(oks);

		int batchsize = oks.length;

		if (allok) Toolbox.output(message + "\t" + batchsize + " sql-transactions in batch." ,null);
		else 
			connection.rollback();

		return allok;
	}

	private String 				statementIDs(DTheorem theorem) {

		String ids = "";
		for (model.logic.Statement s : theorem) ids += s.getID() + ":";			

		DStatement preliminary = theorem.getPreliminary();

		if (preliminary.size() > 0)
			ids += theorem.getPreliminary().getID();

		return ids;
	}
	
	private ArrayList<String> 	fetchStringColumn(String column, String table) {

		ArrayList<String> names = new ArrayList<String>();

		String sql = "SELECT " + column + " FROM " + table + ";";

		try {

			Statement s = connection.createStatement();

			ResultSet result = s.executeQuery(sql);

			while (result.next()) 			
				names.add(result.getString(1));

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return names;
	}
}
