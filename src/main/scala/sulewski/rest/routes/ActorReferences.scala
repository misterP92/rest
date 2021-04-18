package sulewski.rest.routes

import akka.actor.typed.ActorRef
import sulewski.rest.domain.{EndpointApi, UserManagementApi}

final case class ActorReferences(endpointRegistryActor: ActorRef[EndpointApi.BaseCommand],
                                 userRegistryActor: ActorRef[UserManagementApi.BaseLogCommand])