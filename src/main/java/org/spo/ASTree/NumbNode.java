package org.spo.ASTree;

import org.org.spo.Token;

public class NumbNode extends ExprNode {

    public final Token number;

    public NumbNode(Token number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return number.text;
    }
}
