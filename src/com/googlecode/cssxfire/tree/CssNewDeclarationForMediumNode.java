/*
 * Copyright 2010 Ronnie Kolehmainen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.cssxfire.tree;

import com.googlecode.cssxfire.CssUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssNewDeclarationForMediumNode extends CssNewDeclarationNode
{
    protected CssNewDeclarationForMediumNode(@NotNull CssDeclaration cssDeclaration, @NotNull CssElement destinationElement, boolean deleted)
    {
        super(cssDeclaration, destinationElement, deleted);
    }

    private CssBlock ensureSelectorTargetExists()
    {
        CssSelectorNode selectorNode = getCssSelectorNode();
        String selector = selectorNode.getSelector();

        PsiElement element = destinationBlock;
        while ((element = element.getNextSibling()) != null)
        {
            if (element instanceof CssRuleset)
            {
                CssRuleset cssRuleset = (CssRuleset) element;
                CssSelectorList selectorList = cssRuleset.getSelectorList();
                if (selectorList != null && selector.equals(selectorList.getText()))
                {
                    return cssRuleset.getBlock();
                }
            }
        }

        // not found, which is also expected... we have to create a new one

        PsiElement parent = destinationBlock.getParent();
        // if "destinationBlock" is a CssMediumList its parent must be a CssMedia element
        CssRuleset ruleset = CssUtils.createRuleset(destinationBlock.getProject(), selector);
        parent.addAfter(ruleset, destinationBlock);
        return ruleset.getBlock();
    }

    @Override
    public void applyToCode()
    {
        try
        {
            if (isValid() && !deleted)
            {
                CssBlock cssBlock = ensureSelectorTargetExists();

                CssDeclaration[] declarations = cssBlock.getDeclarations();
                CssDeclaration anchor = declarations != null && declarations.length > 0
                        ? declarations[declarations.length - 1]
                        : null;
                cssBlock.addDeclaration(property, value, anchor);
            }
        }
        catch (IncorrectOperationException e)
        {
            e.printStackTrace();
        }
    }
}