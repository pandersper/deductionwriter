package control.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import control.Toolbox;
import model.description.DComposite;
import model.description.DStatement;
import model.description.DTheorem;
import model.logic.Theorem;
import model.logic.Implication.ImplicationType;
import model.logic.abstraction.Formal;

public final class StaticSQL {
	
	
	public static final String 
	
	sessions_insert		= "INSERT INTO Sessions(name, theorems, primitivestable, compositestable, description) VALUES",

	theorems_insert		= "INSERT INTO Theorems(name, statements) VALUES",
	
	statements_insert 	= "INSERT INTO Statements(id, theorem, sequence, implication) VALUES",
	
	primitives_insert 	= "INSERT INTO Primitives(glyphtablename, codepoint, keycode, modifiers) VALUES",
	
	composites_insert 	= "INSERT INTO Composites(glyphtablename, codepoint, codepoints, baselines, keycode, modifiers) VALUES";

	
	public static final String 
	
	sessions_setup 	 = "CREATE TABLE IF NOT EXISTS Sessions(" + 
														"name  VARCHAR(100) NOT NULL, theorems  VARCHAR(300), " + 
					 									"primitivestable  VARCHAR(30) NOT NULL, compositestable  VARCHAR(30) NOT NULL, " + 
					 									"description     VARCHAR(300) NOT NULL, PRIMARY KEY (name));",
					 									
	theorems_setup 	 = "CREATE TABLE IF NOT EXISTS Theorems(" +
					 									"name  VARCHAR(100) NOT NULL, statements  VARCHAR(300), PRIMARY KEY (name));",
														
	statements_setup = "CREATE TABLE IF NOT EXISTS Statements(" + 
														"id  VARCHAR(100) NOT NULL, theorem  VARCHAR(100) NOT NULL, sequence  VARCHAR(2000), " + 
														"implication  INT, FOREIGN KEY (theorem) REFERENCES Theorems(name), PRIMARY KEY (id));",
														
	primitives_setup = "CREATE TABLE IF NOT EXISTS Primitives(" + 
														"glyphtablename  VARCHAR(30) NOT NULL, codepoint  INT NOT NULL, " + 
														"keycode  INT, modifiers  INT, PRIMARY KEY (glyphtablename, codepoint));",
														
	composites_setup = "CREATE TABLE IF NOT EXISTS Composites(" + 
														"glyphtablename  VARCHAR(30) NOT NULL, codepoint  INT NOT NULL, codepoints  VARCHAR(500) NOT NULL, " +
														"baselines  VARCHAR(2000) NOT NULL, keycode  INT, modifiers  INT, PRIMARY KEY (glyphtablename, codepoint));";	


	public static final String
	
	theoremnames_view 		= "CREATE VIEW IF NOT EXISTS Theoremnames AS SELECT name FROM Theorems INTERSECT SELECT name FROM Theorems;",

	primitivestables_view	= "CREATE VIEW IF NOT EXISTS Primitivetables AS SELECT glyphtablename FROM Primitives INTERSECT SELECT glyphtablename FROM Primitives;",

	compositestables_view 	= "CREATE VIEW IF NOT EXISTS Compositetables AS SELECT glyphtablename FROM Composites INTERSECT SELECT glyphtablename FROM Composites;";
	
	
	public static String valuesString(Formal primitive, String viewname) {
		String result = "(";
		result += "'" + viewname + "', " ;
		result += primitive.getCodepoint() + ",";
		result += "0, 0);";
		return result;
	}

	public static String valuesString(DComposite composite, String viewname) {
		String result = "(";
		result += "'" + viewname + "', " ;
		result += "'" + composite.getCodepoint() + "', ";
		result += "'" + composite.codepointsString() + "', ";
		result += "'" + composite.baselinesString() + "', ";
		result += "0, 0);";
		return result;
	}

	public static String valuesString(DStatement statement, Theorem parent) {
		String formals = statement.formalsString();
		String result = "(";
		result += "'" + statement.getID() + "', ";
		result += "'" + parent.getName() + "', ";
		result += "'" + formals + "', ";
		result += statement.implicationID() + ");";
		return result;
	}	
	
	
	public static String[] TheoremToSQL(DTheorem theorem) {

		DStatement preliminary = theorem.getPreliminary();

		String[] sqls = new String[theorem.lengthInStatements()];

		String sql = StaticSQL.statements_insert;

		int i = 0;
		for (DStatement s : theorem) {								///(0B1D)
			sql += StaticSQL.valuesString(s, theorem);
			sqls[i++] = sql;
			sql = StaticSQL.statements_insert;
		}

		if (preliminary.size() > 0) {
			sql += StaticSQL.valuesString(preliminary, theorem);
			sqls[i++] = sql;
		}

		return sqls;
	}

	public static boolean allok(int[] oks) {

		boolean allok = true;

		for (int ok : oks) 
			allok = allok & (ok >= 0);

		return allok;
	}

	public static ArrayList<ImplicationType> parseToImplicationType(ArrayList<Integer> implications) {

		ArrayList<ImplicationType> types = new ArrayList<ImplicationType>();

		for (Integer type : implications) {

			switch (type) {

			case (1):
				types.add(ImplicationType.LEFT);
				break;
			case (2):
				types.add(ImplicationType.RIGHT);
				break;
			case (3):
				types.add(ImplicationType.EQUIV);
				break;
			case (-1):
				types.add(null);
				break;
			default:
				Toolbox.output("not reached",null);
				break;
			}
		}		
		return types;
	}

	
	public static void connectionInfo(Connection connection) throws SQLException {

		System.out.println("Auto-commit: \t\t" + connection.getAutoCommit());

		System.out.println("Connection --");
		System.out.println("Connection open:\t" + !connection.isClosed());
		System.out.println("Client info:\t" + connection.getClientInfo());
		System.out.println("driver name: \t\t" + connection.getMetaData().getDriverName());		
		System.out.println("user: \t\t\t" + connection.getMetaData().getUserName());					
		System.out.println("db url:\t\t\t" + connection.getMetaData().getURL());					
		System.out.println("Network timeout: \t" + connection.getNetworkTimeout());
		System.out.println("open across commit:\t" + connection.getMetaData().supportsOpenStatementsAcrossCommit());
		System.out.println("max connections:\t" + connection.getMetaData().getMaxConnections());

		System.out.println("Filesystem --");
		System.out.println("local files per table:\t" + connection.getMetaData().usesLocalFilePerTable());
		System.out.println("local file:\t\t" + connection.getMetaData().usesLocalFiles());

		System.out.println("Syntax --");
		System.out.println("sql minimal:\t\t" + connection.getMetaData().supportsMinimumSQLGrammar());
		System.out.println("sql core:\t\t" + connection.getMetaData().supportsCoreSQLGrammar());
		System.out.println("sql extended:\t\t" + connection.getMetaData().supportsExtendedSQLGrammar());
		System.out.println("schema term: \t\t" + connection.getMetaData().getSchemaTerm());
		System.out.println("stored functions calls syntax:\t" + connection.getMetaData().supportsStoredFunctionsUsingCallSyntax());
	
		System.out.println("Semantics --");
		System.out.println("group by:\t\t" + connection.getMetaData().supportsGroupBy());
		System.out.println("outer join:\t\t" + connection.getMetaData().supportsOuterJoins());
		System.out.println("full outer join:\t" + connection.getMetaData().supportsFullOuterJoins());
		System.out.println("union:\t\t\t" + connection.getMetaData().supportsUnion());
		System.out.println("union all:\t\t" + connection.getMetaData().supportsUnionAll());
		System.out.println("stored procedures:\t\t" + connection.getMetaData().supportsStoredProcedures()+"\n");
		System.out.println("schemas in procedure calls:\t" + connection.getMetaData().supportsSchemasInProcedureCalls());
	}

}
