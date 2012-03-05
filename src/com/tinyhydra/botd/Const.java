package com.tinyhydra.botd;


/**
 * Brew of the day
 * Copyright (C) 2012  tinyhydra.com
 * *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Constants.
//TODO: while it's easier to deal with these strings this way,
//TODO: it's probably better to set them in values.xml. Food for thought
public class Const {
    public final static String GenPrefs = "BotdGenPrefs";

    public final static String LastVoteDate = "LastVoteDate";
    public final static String LastVotedFor = "LastVotedFor";

    public final static String LastTopTenQueryTime = "LastTopTenQueryTime";
    public final static String LastTopTenQueryResults = "LastTopTenQueryResults";

    public final static int CODE_SHOWTOAST = 0;
    public final static int CODE_GETTOPTEN = 1;

    public final static String MessageToastString = "MessageToastString";
}
