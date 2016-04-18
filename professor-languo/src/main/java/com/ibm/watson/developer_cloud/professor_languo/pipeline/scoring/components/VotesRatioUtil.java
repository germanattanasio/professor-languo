/*
 * Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.watson.developer_cloud.professor_languo.pipeline.scoring.components;

public class VotesRatioUtil {
  public static double calculateVoteRatio(long upVotes, long downVotes, int factor) {
    upVotes = upVotes > 0 ? upVotes : 1; // prevent divide by zero error
    downVotes = downVotes > 0 ? downVotes : 1;// prevent divide by zero
    // error
    return (upVotes / downVotes) * ((upVotes / factor) + 1);
  }
}
