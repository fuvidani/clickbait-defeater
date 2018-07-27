var matches = [];
var elems = document.getElementsByTagName("*");
for (var i=0; i<elems.length; i++) {
  if (elems[i].id.indexOf("hyperfeed_story_id") == 0)
    matches.push(elems[i]);
}

console.log(matches);
