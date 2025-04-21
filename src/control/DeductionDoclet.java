package control;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import com.sun.source.util.DocTrees;
import com.sun.source.doctree.DocCommentTree;

import javax.tools.Diagnostic.Kind;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

public class DeductionDoclet implements Doclet {

	Reporter reporter;

	public void init(Locale locale, Reporter reporter) {
        reporter.print(Kind.NOTE, "Doclet using locale: " + locale);
        this.reporter = reporter;
    }

    public void printElement(DocTrees trees, Element e) {
        DocCommentTree docCommentTree = trees.getDocCommentTree(e);
        if (docCommentTree != null) {
            System.out.println("Element (" + e.getKind() + ": "
                    + e + ") has the following comments:");
            System.out.println("Entire body: " + docCommentTree.getFullBody());
            System.out.println("Block tags: " + docCommentTree.getBlockTags());
        }
    }

    public boolean run(DocletEnvironment docEnv) {
        reporter.print(Kind.NOTE, "overviewfile: " + overviewfile);

        DocTrees docTrees = docEnv.getDocTrees();

//        try {
//            Element e = ElementFilter.typesIn(docEnv.getSpecifiedElements()).iterator().next();
//            DocCommentTree docCommentTree
//                    = docTrees.getDocCommentTree(e, overviewfile);
//            if (docCommentTree != null) {
//                System.out.println("Overview html: " + docCommentTree.getFullBody());
//            }
//        } catch (IOException missing) {
//            reporter.print(Kind.ERROR, "No overview.html found.");
//        }

        for (TypeElement t : ElementFilter.typesIn(docEnv.getIncludedElements())) {
            System.out.println(t.getKind() + ":" + t);
            for (Element e : t.getEnclosedElements()) {
                printElement(docTrees, e);
            }
        }
        return true;
    }

    public String getName() {
        return "Example";
    }

    private String overviewfile = "E:/src/java/eclipse-workspace/DeductionWriter/doc/overview.html";

    public Set<? extends Option> getSupportedOptions() {
        Option[] options = {
            new Option() {
                private final List<String> someOption = Arrays.asList(
                        "-overviewfile",
                        "--overview-file",
                        "-o"
                );

                
                public int getArgumentCount() {
                    return 1;
                }

                
                public String getDescription() {
                    return "an option with aliases";
                }

                
                public Option.Kind getKind() {
                    return Option.Kind.STANDARD;
                }

                
                public List<String> getNames() {
                    return someOption;
                }

                
                public String getParameters() {
                    return "file";
                }

                
                public boolean process(String opt, List<String> arguments) {
//                    overviewfile = arguments.get(0);
                    return true;
                }
            }
        };
        return new HashSet<>(Arrays.asList(options));
    }

    
    public SourceVersion getSupportedSourceVersion() {

    	return SourceVersion.latest();
    }
 }
 