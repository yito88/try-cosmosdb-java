function insertIfNotExists(itemToCreate) {
  var container = getContext().getCollection();
  var query = {query: "SELECT * FROM Record r WHERE r.key1 = @id", parameters: [{name: "@id", value: itemToCreate.id}]};

  var isAccepted = container.queryDocuments(
      container.getSelfLink(),
      query,
  function (err, items, options) {
      if (err) throw err;

      if (!items || !items.length) {
          var accepted = container.createDocument(container.getSelfLink(), itemToCreate);
          if (!accepted) throw new Error('Failed to insert');
      } else {
          getContext().getResponse();
      }
  });
  if (!isAccepted) throw new Error('The query was not accepted by the server.');
}
