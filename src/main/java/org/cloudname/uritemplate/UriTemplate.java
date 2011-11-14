package org.cloudname.uritemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Store an uri template, and make it expandable by a variable definition hash.
 * For details, see the URI templates project at http://code.google.com/p/uri-templates/
 *
 * @author vlarsen
 */
public class UriTemplate {
    private String template;
    private ErrorState errorState = new ErrorState();

    /**
     * Construct an UriTemplate with the given pattern.
     * @param t The template pattern.
     */
    public UriTemplate(String t) { template = t; }

    public ErrorState getErrorState() { return errorState; }

    /**
     * Expand the stored template using the provided variable mappings.
     * If an error occurs, the errorState will be set to an object describing the error.
     *
     * @param vars A map of variable names to values
     * @return String with the expanded template.
     */
    public String expand(Map<String,String> vars) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < template.length(); i++) {
            // look for the start of an expression
            if (template.charAt(i) != '{') {
                // not an expression, so copy verbatim
                sb.append(template.charAt(i));
                continue;
            }

            int exprStart = i+1;
            int exprEnd = template.indexOf('}', exprStart);
            if (exprEnd != -1) {
                final String expression = template.substring(exprStart, exprEnd);
                sb.append(expandExpression(expression, vars));

                // we extracted the expression, so continue after it
                i = exprEnd;

            } else {
                // We found an error, so set it
                sb.append(template.substring(exprStart));
                errorState.set("Expression not closed", exprStart);
            }

        }
        // return the complete expanded template
        return sb.toString();
    }

    /**
     * Expand an expression in the context of a variable map.
     * The algorithm is due to the URI-templates RFC (draft) found at http://tools.ietf.org/html/draft-gregorio-uritemplate-07
     *
     * @param expression A string starting with an optional one-char operator, and continuing with a varspec.
     *        The varspec is a comma-separated list of variable names with modifiers.
     * @param vars A map of variable names to variable values.
     * @return The final expanded string
     */
    private static String expandExpression(String expression, Map<String,String> vars) {
        Character operator = expression.charAt(0);  // the first character may be an operator
        int varspecStart = 0;  // the varspec tentatively starts here

        if (params.containsKey(operator)) {
            // found an operator we know about, so skip it to find the varspec
            varspecStart += 1;
        } else {
            operator = null;
        }

        // look up the algorithm parameters, and pick apart the varspec
        OperatorParams operatorParams = params.get(operator);
        String varspecString = expression.substring(varspecStart);
        String[] varspec = varspecString.split(",");

        // Build the result string
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String var : varspec) {
            if (vars.containsKey(var)) {
                // the variable is defined, so do something...

                // insert the separator, taking care of first-occurance
                sb.append(first ? operatorParams.first : operatorParams.sep);
                first = false;

                // add variable name if the operator demands it
                if (operatorParams.named) {
                    sb.append(var);
                    if (vars.get(var).isEmpty()) {
                        sb.append(operatorParams.ifemp);
                        continue;
                    } else {
                        sb.append("=");
                    }
                }

                // append the actual variable value
                sb.append(vars.get(var));
            }
        }

        // return the completely expanded string
        return sb.toString();
    }

    /**
     * Hold the error state of a template expansion.
     */
    public static class ErrorState {
        private boolean error = false;
        private String msg;
        private int at = 0;

        /**
         * Set the state to an error.
         * @param m The message to describe the error.
         * @param a The position the error is at.
         */
        public void set(String m, int a) {
            error = true;
            msg = m;
            at = a;
        }

        /** Check if this error-state describes an error */
        public boolean isError() { return error; }

        /** Get the position the error is at */
        public int getAt() { return at; }
    }

    /** Global map of operator-parameters, keyed on operator. Used to retrieve the specific params for a given operator.
     * The contents of this "table" is taken from the RFC.
     */
    private final static Map<Character,OperatorParams> params = new HashMap<Character, OperatorParams>();
    static {
        params.put(null, new OperatorParams("",  ",", false, "",  OperatorParams.Allow.U));
        params.put( '+', new OperatorParams("",  ",", false, "",  OperatorParams.Allow.U_AND_R));
        params.put( '.', new OperatorParams(".", ".", false, "",  OperatorParams.Allow.U));
        params.put( '/', new OperatorParams("/", "/", false, "",  OperatorParams.Allow.U));
        params.put( ';', new OperatorParams(";", ";", true,  "",  OperatorParams.Allow.U));
        params.put( '?', new OperatorParams("?", "&", true,  "=", OperatorParams.Allow.U));
        params.put( '&', new OperatorParams("&", "&", true,  "=", OperatorParams.Allow.U));
        params.put( '#', new OperatorParams("#", ",", false, "",  OperatorParams.Allow.U_AND_R));
    }


    /**
     * Holds a set of parameters that is defining for each different operator.
     * This is basically a table-row of values to use in the expanding algorithm.
     */
    private static class OperatorParams {
        public enum Allow {U, U_AND_R}

        public final String first;
        public final String sep;
        public final Boolean named;
        public final String ifemp;
        public final Allow allow;

        public OperatorParams(String f, String s, Boolean n, String i, Allow a) {
            first = f;
            sep = s;
            named = n;
            ifemp = i;
            allow = a;
        }

    }
}
