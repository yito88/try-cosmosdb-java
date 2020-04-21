function putIfExists(itemToUpdate) {
  var container = getContext().getCollection();
  var query = {"query": "SELECT * FROM Record r WHERE r.key1 = @key1 AND r.key2 = @key2",
               "parameters": [{"name": "@key1", "value": itemToUpdate.key1},
                              {"name": "@key2", "value": itemToUpdate.key2}]};

  var isAccepted = container.queryDocuments(
      container.getSelfLink(),
      query,
      function (err, items, options) {
          if (err) throw err;

          if (!items || !items.length) {
            throw new Error("Does not exist");
          } else {
            if (items.length != 1) throw new Error("Unable to find both names");
            var accepted = container.replaceDocument(items[0]._self, itemToUpdate);
            if (!accepted) throw new Error("Failed to update");
          }
      });
  if (!isAccepted) throw new Error("The query was not accepted by the server.");
}
