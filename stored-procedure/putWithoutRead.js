function putWithoutRead(itemToCreate) {
  var container = getContext().getCollection();

  var isAccepted = container.createDocument(container.getSelfLink(), itemToCreate);

  if (!isAccepted) throw new Error("The query was not accepted by the server.");
}
