// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random Fact about Yaa to the page.
 */
function addRandomFact() {
  const facts =
      ['I don\'t have any pets. ', 'My birthday is in November.', 'I have 2 older brothers.', 'I love to cook!'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

// Adds the user's comments to the page 
function getMessage() {
  fetch('/data').then(response => response.text()).then((messages) => {
    document.getElementById('comment').innerHTML = messages;
  });
}

