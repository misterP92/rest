package sulewski.rest.routes

import akka.actor.typed.ActorRef
import sulewski.rest.domain.{EndpointApi, ServerInfoApi, UserManagementApi}

final case class ActorReferences(endpointRegistryActor: ActorRef[EndpointApi.BaseCommand],
                                 userLogRegistryActor: ActorRef[UserManagementApi.BaseLogCommand],
                                 serverInfoRegistryActor: ActorRef[ServerInfoApi.ServerInfoBaseCommand])