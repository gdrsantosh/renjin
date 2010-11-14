/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.interpreter;

import r.lang.EnvExp;
import r.lang.SymbolExp;

import java.util.ArrayList;

public class SymbolCompletion implements NameCompletion {

  private final EnvExp global;

  public SymbolCompletion(EnvExp global) {
    this.global = global;
  }

  @Override
  public String[] completeName(String part) {
    ArrayList<String> list = new ArrayList<String>();
    for(EnvExp env : global.selfAndParents()) {
      for(SymbolExp name : env.getSymbolNames()) {
        if(name.getPrintName().startsWith(part)) {
          list.add(name.getPrintName());
        }
      }
    }
    return list.toArray(new String[list.size()]);
  }
}
