function putIfNotExists(itemToCreate) {
  var container = getContext().getCollection();
  var query = {"query": "SELECT * FROM Record r WHERE r.key1 = @key1 AND r.key2 = @key2",
               "parameters": [{"name": "@key1", "value": itemToCreate.key1},
                              {"name": "@key2", "value": itemToCreate.key2}]};

  var isAccepted = container.queryDocuments(
      container.getSelfLink(),
      query,
      function (err, items, options) {
          if (err) throw err;

          if (!items || !items.length) {
            var accepted = container.createDocument(container.getSelfLink(), itemToCreate);
            if (!accepted) throw new Error("Failed to insert");
          } else {
            throw new Error("Already exists");
          }
      });
  if (!isAccepted) throw new Error("The query was not accepted by the server.");
}
